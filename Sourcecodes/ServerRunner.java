package com.travelease;

import com.travelease.servlet.*;
import com.travelease.util.DBConnection;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.net.ServerSocket;

/**
 * ServerRunner - Standalone Apache Tomcat 10.1 Launcher
 * 
 * Runs the Jakarta Servlet backend without requiring a separate manual Tomcat installation.
 * Run via Maven: mvn compile exec:java
 * Or run directly in VS Code / Eclipse / IntelliJ as standard Java Application.
 */
public class ServerRunner {

    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "/travelease";

    public static void main(String[] args) {
        // Enforce IPv4 stack for reliable local socket binding
        System.setProperty("java.net.preferIPv4Stack", "true");

        System.out.println("==================================================");
        System.out.println("   Starting TravelEase Jakarta Servlet Backend    ");
        System.out.println("   Target Environment: Embedded Apache Tomcat 10.1");
        System.out.println("==================================================");

        // 1. Test MySQL Database Connectivity
        boolean dbOk = DBConnection.testConnection();
        if (!dbOk) {
            System.err.println("--------------------------------------------------");
            System.err.println("WARNING: Could not connect to MySQL Server!");
            System.err.println("1. Ensure MySQL Server 8.0 is running on your machine.");
            System.err.println("2. Ensure 'schema.sql' was executed in MySQL Workbench.");
            System.err.println("3. Check credentials in: src/main/resources/db.properties");
            System.err.println("--------------------------------------------------");
        }

        // 2. Verify Port 8080 Availability
        if (!isPortAvailable(PORT)) {
            System.err.println("--------------------------------------------------");
            System.err.println("[Tomcat ERROR] Port " + PORT + " is already occupied by another process!");
            System.err.println("A previous ServerRunner or web server instance is already running.");
            System.err.println("To check which process is on port " + PORT + ":");
            System.err.println("   cmd: netstat -ano | findstr :" + PORT);
            System.err.println("To terminate all running Java processes:");
            System.err.println("   cmd: taskkill /F /IM java.exe");
            System.err.println("--------------------------------------------------");
            return;
        }

        try {
            // 3. Configure Embedded Tomcat
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(PORT);
            tomcat.getConnector(); // Initialize default HTTP NIO connector

            // Base directory for Tomcat work / temp files
            File baseDirFile = new File(System.getProperty("java.io.tmpdir"), "travelease-tomcat");
            if (!baseDirFile.exists()) {
                baseDirFile.mkdirs();
            }
            tomcat.setBaseDir(baseDirFile.getAbsolutePath());

            // 4. Resolve webapp document base directory (supports running from root or backend/)
            File docBase = new File("backend/src/main/webapp");
            if (!docBase.exists()) {
                docBase = new File("src/main/webapp");
            }
            if (!docBase.exists()) {
                docBase = new File(baseDirFile, "webapp");
                docBase.mkdirs();
            }

            Context ctx = tomcat.addContext(CONTEXT_PATH, docBase.getAbsolutePath());

            // 5. Register All Jakarta Servlets
            Tomcat.addServlet(ctx, "TripSearchServlet", new TripSearchServlet());
            ctx.addServletMappingDecoded("/api/trips", "TripSearchServlet");

            Tomcat.addServlet(ctx, "SeatAvailabilityServlet", new SeatAvailabilityServlet());
            ctx.addServletMappingDecoded("/api/seats", "SeatAvailabilityServlet");

            Tomcat.addServlet(ctx, "BookingServlet", new BookingServlet());
            ctx.addServletMappingDecoded("/api/bookings/*", "BookingServlet");

            Tomcat.addServlet(ctx, "AuthServlet", new AuthServlet());
            ctx.addServletMappingDecoded("/api/auth/*", "AuthServlet");

            Tomcat.addServlet(ctx, "AdminServlet", new AdminServlet());
            ctx.addServletMappingDecoded("/api/admin/*", "AdminServlet");

            System.out.println("[Tomcat] Server initialized on port " + PORT);
            System.out.println("[Tomcat] Base URL: http://localhost:" + PORT + CONTEXT_PATH);
            System.out.println("[Tomcat] API Endpoints ready:");
            System.out.println("         - GET  http://localhost:" + PORT + CONTEXT_PATH + "/api/trips");
            System.out.println("         - GET  http://localhost:" + PORT + CONTEXT_PATH + "/api/seats?tripId=1");
            System.out.println("         - POST http://localhost:" + PORT + CONTEXT_PATH + "/api/bookings");
            System.out.println("         - POST http://localhost:" + PORT + CONTEXT_PATH + "/api/auth/login");
            System.out.println("         - GET  http://localhost:" + PORT + CONTEXT_PATH + "/api/admin/stats");
            System.out.println("==================================================");

            // 6. Start Embedded Tomcat Engine & Await Requests
            tomcat.start();
            tomcat.getServer().await();

        } catch (Exception e) {
            System.err.println("[Tomcat ERROR] Failed to start embedded server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
