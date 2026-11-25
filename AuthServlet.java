package com.travelease.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.travelease.dao.UserDAO;
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

/**
 * AuthServlet - Authentication, Session Management, & Registration
 * Endpoints:
 *   POST /api/auth/register
 *   POST /api/auth/login
 *   POST /api/auth/logout
 *   GET  /api/auth/me
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        if ("/me".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                JsonUtil.sendSuccess(resp, "Authenticated user session active", user);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "No active session.");
            }
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();

        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            JsonUtil.sendSuccess(resp, "Logged out successfully", null);
            return;
        }

        // Read JSON body
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

        if ("/login".equals(path)) {
            handleLogin(body, req, resp);
        } else if ("/register".equals(path)) {
            handleRegister(body, req, resp);
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown auth endpoint.");
        }
    }

    private void handleLogin(JsonObject body, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!body.has("email") || !body.has("password")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
            return;
        }

        String email = body.get("email").getAsString();
        String password = body.get("password").getAsString();

        try {
            User user = userDAO.authenticate(email, password);
            if (user != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", user);
                JsonUtil.sendSuccess(resp, "Login successful", user);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
            }
        } catch (SQLException e) {
            System.err.println("[AuthServlet] Login error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during authentication.");
        }
    }

    private void handleRegister(JsonObject body, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!body.has("name") || !body.has("email") || !body.has("password")) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Name, email, and password are required.");
            return;
        }

        String name = body.get("name").getAsString();
        String email = body.get("email").getAsString();
        String password = body.get("password").getAsString();
        String role = body.has("role") ? body.get("role").getAsString() : "USER";

        if (password.length() < 6) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "An account with this email already exists.");
                return;
            }

            User newUser = userDAO.register(name, email, password, role);
            if (newUser != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("user", newUser);
                JsonUtil.sendJsonResponse(resp, HttpServletResponse.SC_CREATED, newUser);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register user.");
            }
        } catch (SQLException e) {
            System.err.println("[AuthServlet] Registration error: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error during registration.");
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
        System.out.println("[Info] AuthServlet is managed by Apache Tomcat.");
        System.out.println("[Info] Starting full embedded Tomcat 10.1 server on port 8080 via ServerRunner...");
        com.travelease.ServerRunner.main(args);
    }
}
