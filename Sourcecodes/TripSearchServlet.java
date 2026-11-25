package com.travelease.servlet;

import com.travelease.dao.TripDAO;
import com.travelease.model.Trip;
import com.travelease.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * TripSearchServlet - Handles Trip Query & Search HTTP Requests
 * Endpoint: GET /api/trips
 * Parameters: source, destination, type (BUS/TRAIN/ALL), date (YYYY-MM-DD)
 */
@WebServlet("/api/trips")
public class TripSearchServlet extends HttpServlet {

    private final TripDAO tripDAO = new TripDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String source = req.getParameter("source");
        String destination = req.getParameter("destination");
        String type = req.getParameter("type");
        String timeDep = req.getParameter("timeDep");
        if (timeDep == null || timeDep.trim().isEmpty()) {
            timeDep = req.getParameter("departureTime");
        }

        try {
            List<Trip> trips = tripDAO.searchTrips(source, destination, type, timeDep);
            JsonUtil.sendSuccess(resp, "Trips retrieved successfully", trips);
        } catch (SQLException e) {
            System.err.println("[TripSearchServlet] Database error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error retrieving trips.");
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
        System.out.println("[Info] TripSearchServlet is managed by Apache Tomcat.");
        System.out.println("[Info] Starting full embedded Tomcat 10.1 server on port 8080 via ServerRunner...");
        com.travelease.ServerRunner.main(args);
    }
}
