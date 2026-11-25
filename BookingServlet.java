package com.travelease.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.travelease.dao.BookingDAO;
import com.travelease.model.Booking;
import com.travelease.model.Passenger;
import com.travelease.model.User;
import com.travelease.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingServlet - Handles Transactional Reservations & Cancellations
 * Endpoints:
 *   GET  /api/bookings          (List user bookings)
 *   POST /api/bookings          (Create atomic reservation)
 *   POST /api/bookings/cancel   (Cancel booking & release seats)
 */
@WebServlet("/api/bookings/*")
public class BookingServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // Fallback for educational development if frontend sends ?userId=X
        int userId = 1;
        if (currentUser != null) {
            userId = currentUser.getUserId();
        } else {
            String userIdParam = req.getParameter("userId");
            if (userIdParam != null) {
                try {
                    userId = Integer.parseInt(userIdParam);
                } catch (NumberFormatException ignored) {}
            }
        }

        try {
            List<Booking> bookings = bookingDAO.getUserBookings(userId);
            JsonUtil.sendSuccess(resp, "User bookings retrieved", bookings);
        } catch (SQLException e) {
            System.err.println("[BookingServlet] Database error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving bookings.");
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing request payload.");
            return;
        }

        JsonObject body = JsonParser.parseString(sb.toString()).getAsJsonObject();

        // Handle Cancellation Request
        if ("/cancel".equals(path)) {
            handleCancellation(body, req, resp);
            return;
        }

        // Handle New Reservation Creation
        handleCreateBooking(body, req, resp);
    }

    private void handleCreateBooking(JsonObject body, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        int userId = 1; // Default test traveler
        if (currentUser != null) {
            userId = currentUser.getUserId();
        } else if (body.has("userId")) {
            userId = body.get("userId").getAsInt();
        }

        if (!body.has("tripId") || !body.has("seats")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "tripId and seats are required.");
            return;
        }

        int tripId = body.get("tripId").getAsInt();
        String journeyDate = body.has("journeyDate") && !body.get("journeyDate").getAsString().trim().isEmpty()
                             ? body.get("journeyDate").getAsString()
                             : java.time.LocalDate.now().toString();

        List<String> seats = new ArrayList<>();
        JsonArray seatsArr = body.getAsJsonArray("seats");
        for (JsonElement el : seatsArr) {
            seats.add(el.getAsString());
        }

        List<Passenger> passengers = new ArrayList<>();
        if (body.has("passengers")) {
            JsonArray passArr = body.getAsJsonArray("passengers");
            for (JsonElement el : passArr) {
                JsonObject pObj = el.getAsJsonObject();
                passengers.add(new Passenger(
                    pObj.has("name") ? pObj.get("name").getAsString() : "Passenger",
                    pObj.has("age") ? pObj.get("age").getAsInt() : 25,
                    pObj.has("gender") ? pObj.get("gender").getAsString() : "Male",
                    pObj.has("seat_number") ? pObj.get("seat_number").getAsString() : seats.get(0)
                ));
            }
        }

        try {
            // Execute atomic transaction in BookingDAO
            Booking newBooking = bookingDAO.createBooking(userId, tripId, journeyDate, seats, passengers);
            JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, newBooking);
        } catch (IllegalStateException e) {
            // Concurrency or seat conflict error (HTTP 409 Conflict)
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            System.err.println("[BookingServlet] Transaction failed: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Reservation transaction failed.");
        }
    }

    private void handleCancellation(JsonObject body, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!body.has("bookingReference")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "bookingReference is required.");
            return;
        }

        String ref = body.get("bookingReference").getAsString();
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        int userId = (currentUser != null) ? currentUser.getUserId() : 1;
        boolean isAdmin = (currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole()));

        try {
            boolean success = bookingDAO.cancelBooking(ref, userId, isAdmin);
            if (success) {
                JsonUtil.sendSuccess(resp, "Reservation cancelled successfully and seats released.", null);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Booking reference not found.");
            }
        } catch (SecurityException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            System.err.println("[BookingServlet] Cancellation error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during cancellation.");
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
        System.out.println("[Info] BookingServlet is managed by Apache Tomcat.");
        System.out.println("[Info] Starting full embedded Tomcat 10.1 server on port 8080 via ServerRunner...");
        com.travelease.ServerRunner.main(args);
    }
}
