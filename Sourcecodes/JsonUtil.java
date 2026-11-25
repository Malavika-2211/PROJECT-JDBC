package com.travelease.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonUtil - JSON Helper and HTTP Response Utility
 * 
 * Uses Google Gson to serialize Java objects into standard JSON strings
 * and configure HTTP responses with proper Content-Type and CORS headers.
 */
public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    public static Gson getGson() {
        return gson;
    }

    /**
     * Sends an HTTP response formatted as application/json.
     */
    public static void sendJsonResponse(HttpServletResponse resp, int statusCode, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(statusCode);

        // CORS headers so React (localhost:5173) can communicate during development
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }

    /**
     * Sends a simple success response: { "success": true, "message": "..." }
     */
    public static void sendSuccess(HttpServletResponse resp, String message, Object payload) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        if (payload != null) {
            map.put("data", payload);
        }
        sendJsonResponse(resp, HttpServletResponse.SC_OK, map);
    }

    /**
     * Sends an error response: { "success": false, "error": "..." }
     */
    public static void sendError(HttpServletResponse resp, int statusCode, String errorMessage) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("error", errorMessage);
        sendJsonResponse(resp, statusCode, map);
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     TravelEase: Google Gson JSON Serialization   ");
        System.out.println("==================================================");
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("appName", "TravelEase");
        sampleData.put("status", "ACTIVE");
        sampleData.put("version", "1.0.0");
        sampleData.put("technologies", new String[]{"Jakarta Servlets 6.0", "Embedded Tomcat 10.1", "MySQL 8.0"});
        
        String json = gson.toJson(sampleData);
        System.out.println("Serialized JSON Output:");
        System.out.println(json);
        System.out.println("\n[SUCCESS] Gson JSON utility verified!");
        System.out.println("==================================================");
    }
}
