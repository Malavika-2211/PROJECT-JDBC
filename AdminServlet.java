package com.travelease.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.travelease.dao.BookingDAO;
import com.travelease.dao.TripDAO;
import com.travelease.model.Booking;
import com.travelease.model.Trip;
import com.travelease.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AdminServlet - Administrator Console & Operations
 * Endpoints:
 *   GET  /api/admin/stats     (High-level metrics)
 *   GET  /api/admin/trips     (All trips)
 *   POST /api/admin/trips     (Create trip & auto-seed seats)
 *   POST /api/admin/trips/toggle (Deactivate / Activate trip)
 *   GET  /api/admin/bookings  (All customer reservations)
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final TripDAO tripDAO = new TripDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        try {
            if ("/stats".equals(path)) {
                handleStats(resp);
            } else if ("/trips".equals(path)) {
                List<Trip> trips = tripDAO.getAllTrips();
                JsonUtil.sendSuccess(resp, "All trips retrieved", trips);
            } else if ("/bookings".equals(path)) {
                List<Booking> bookings = bookingDAO.getAllBookings();
                JsonUtil.sendSuccess(resp, "All reservations retrieved", bookings);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminServlet] Error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        if (sb.length() == 0) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing request body.");
            return;
        }

        JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();

        try {
            if ("/trips".equals(path)) {
                handleAddTrip(body, resp);
            } else if ("/trips/toggle".equals(path)) {
                handleToggleTrip(body, resp);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown admin endpoint.");
            }
        } catch (SQLException e) {
            System.err.println("[AdminServlet] Error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private void handleStats(HttpServletResponse resp) throws SQLException, IOException {
        List<Trip> allTrips = tripDAO.getAllTrips();
        List<Booking> allBookings = bookingDAO.getAllBookings();

        long activeTrips = allTrips.stream().filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus())).count();
        long confirmedBookings = allBookings.stream().filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus())).count();

        BigDecimal grossRevenue = allBookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus()))
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrips", allTrips.size());
        stats.put("activeTrips", activeTrips);
        stats.put("totalBookings", allBookings.size());
        stats.put("confirmedBookings", confirmedBookings);
        stats.put("grossRevenue", grossRevenue);

        JsonUtil.sendSuccess(resp, "Admin stats calculated", stats);
    }

    private void handleAddTrip(JsonObject body, HttpServletResponse resp) throws SQLException, IOException {
        Trip trip = new Trip();
        trip.setTransportType(body.get("transportType").getAsString());
        trip.setServiceName(body.get("serviceName").getAsString());
        trip.setSource(body.get("source").getAsString());
        trip.setDestination(body.get("destination").getAsString());
        trip.setDepartureTime(body.get("departureTime").getAsString());
        trip.setArrivalTime(body.get("arrivalTime").getAsString());
        trip.setDuration(body.get("duration").getAsString());
        trip.setFare(body.get("fare").getAsBigDecimal());
        trip.setTotalSeats(body.get("totalSeats").getAsInt());
        trip.setStatus("ACTIVE");

        boolean success = tripDAO.addTrip(trip);
        if (success) {
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, trip);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to schedule trip.");
        }
    }

    private void handleToggleTrip(JsonObject body, HttpServletResponse resp) throws SQLException, IOException {
        int tripId = body.get("tripId").getAsInt();
        String currentStatus = body.has("status") ? body.get("status").getAsString() : "ACTIVE";
        String newStatus = "ACTIVE".equalsIgnoreCase(currentStatus) ? "INACTIVE" : "ACTIVE";

        boolean success = tripDAO.updateTripStatus(tripId, newStatus);
        if (success) {
            JsonUtil.sendSuccess(resp, "Trip status updated to " + newStatus, null);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Trip not found.");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[Info] AdminServlet is managed by Apache Tomcat.");
        System.out.println("[Info] Starting full embedded Tomcat 10.1 server on port 8080 via ServerRunner...");
        com.travelease.ServerRunner.main(args);
    }
}
