package com.travelease.servlet;

import com.travelease.dao.SeatDAO;
import com.travelease.model.Seat;
import com.travelease.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * SeatAvailabilityServlet - Returns dynamic seat layout for a specific trip and date
 * Endpoint: GET /api/seats?tripId=1&date=2026-10-24
 */
@WebServlet("/api/seats")
public class SeatAvailabilityServlet extends HttpServlet {

    private final SeatDAO seatDAO = new SeatDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tripIdParam = req.getParameter("tripId");
        String dateParam = req.getParameter("date");

        if (tripIdParam == null || tripIdParam.trim().isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required query parameter: tripId");
            return;
        }

        try {
            int tripId = Integer.parseInt(tripIdParam);
            String journeyDate = (dateParam != null && !dateParam.trim().isEmpty()) 
                                 ? dateParam 
                                 : java.time.LocalDate.now().toString();

            List<Seat> seats = seatDAO.getSeatsForTripAndDate(tripId, journeyDate);
            JsonUtil.sendSuccess(resp, "Seats retrieved successfully", seats);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid tripId format.");
        } catch (SQLException e) {
            System.err.println("[SeatAvailabilityServlet] Database error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error retrieving seats.");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("[Info] SeatAvailabilityServlet is managed by Apache Tomcat.");
        System.out.println("[Info] Starting full embedded Tomcat 10.1 server on port 8080 via ServerRunner...");
        com.travelease.ServerRunner.main(args);
    }
}
