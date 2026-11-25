package com.travelease.dao;

import com.travelease.model.Trip;
import com.travelease.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TripDAO - Data Access Object for Scheduled Transport Trips
 * 
 * Demonstrates:
 * 1. PreparedStatement parameterized queries (preventing SQL injection).
 * 2. Dynamic seat availability calculation: computes available seats by 
 *    subtracting confirmed bookings for a specific journey date.
 */
public class TripDAO {

    /**
     * Searches for active trips matching source, destination, transport type, and journey date.
     */
    public List<Trip> searchTrips(String source, String destination, String transportType, String timeDep) throws SQLException {
        List<Trip> trips = new ArrayList<>();

        // SQL: calculates available_seats dynamically from active bookings
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t.*, ")
           .append("  (t.total_seats - COALESCE(booked_seats.booked_count, 0)) AS available_seats ")
           .append("FROM trips t ")
           .append("LEFT JOIN ( ")
           .append("  SELECT b.trip_id, COUNT(DISTINCT bs.seat_id) AS booked_count ")
           .append("  FROM bookings b ")
           .append("  JOIN booking_seats bs ON b.booking_id = bs.booking_id ")
           .append("  WHERE b.status = 'CONFIRMED' ")
           .append("  GROUP BY b.trip_id ")
           .append(") booked_seats ON t.trip_id = booked_seats.trip_id ")
           .append("WHERE t.status = 'ACTIVE' ");

        List<Object> params = new ArrayList<>();

        if (source != null && !source.trim().isEmpty()) {
            sql.append("AND LOWER(t.source) LIKE ? ");
            params.add("%" + source.trim().toLowerCase() + "%");
        }

        if (destination != null && !destination.trim().isEmpty()) {
            sql.append("AND LOWER(t.destination) LIKE ? ");
            params.add("%" + destination.trim().toLowerCase() + "%");
        }

        if (transportType != null && !transportType.equalsIgnoreCase("ALL")) {
            sql.append("AND t.transport_type = ? ");
            params.add(transportType.toUpperCase());
        }

        if (timeDep != null && !timeDep.trim().isEmpty() && !timeDep.equalsIgnoreCase("ALL")) {
            if (timeDep.equalsIgnoreCase("MORNING")) {
                sql.append("AND (t.departure_time LIKE '%AM%' AND t.departure_time NOT LIKE '12:%') ");
            } else if (timeDep.equalsIgnoreCase("AFTERNOON")) {
                sql.append("AND (t.departure_time LIKE '%PM%' AND (t.departure_time LIKE '12:%' OR t.departure_time LIKE '01:%' OR t.departure_time LIKE '02:%' OR t.departure_time LIKE '03:%' OR t.departure_time LIKE '04:%')) ");
            } else if (timeDep.equalsIgnoreCase("EVENING")) {
                sql.append("AND (t.departure_time LIKE '%PM%' AND (t.departure_time LIKE '05:%' OR t.departure_time LIKE '06:%' OR t.departure_time LIKE '07:%' OR t.departure_time LIKE '08:%' OR t.departure_time LIKE '09:%' OR t.departure_time LIKE '10:%' OR t.departure_time LIKE '11:%')) ");
            } else {
                sql.append("AND LOWER(t.departure_time) LIKE ? ");
                params.add("%" + timeDep.trim().toLowerCase() + "%");
            }
        }

        sql.append("ORDER BY t.departure_time ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(mapResultSetToTrip(rs));
                }
            }
        }
        return trips;
    }

    /**
     * Retrieves all trips for the Admin portal (both ACTIVE and INACTIVE).
     */
    public List<Trip> getAllTrips() throws SQLException {
        List<Trip> trips = new ArrayList<>();
        String sql = "SELECT t.*, t.total_seats AS available_seats FROM trips t ORDER BY t.trip_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trips.add(mapResultSetToTrip(rs));
            }
        }
        return trips;
    }

    /**
     * Retrieves a single trip by ID.
     */
    public Trip getTripById(int tripId) throws SQLException {
        String sql = "SELECT t.*, t.total_seats AS available_seats FROM trips t WHERE t.trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTrip(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserts a new trip into the database (Admin operation).
     */
    public boolean addTrip(Trip trip) throws SQLException {
        String sql = "INSERT INTO trips (transport_type, service_name, source, destination, " +
                     "departure_time, arrival_time, duration, fare, total_seats, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, trip.getTransportType());
            ps.setString(2, trip.getServiceName());
            ps.setString(3, trip.getSource());
            ps.setString(4, trip.getDestination());
            ps.setString(5, trip.getDepartureTime());
            ps.setString(6, trip.getArrivalTime());
            ps.setString(7, trip.getDuration());
            ps.setBigDecimal(8, trip.getFare());
            ps.setInt(9, trip.getTotalSeats());
            ps.setString(10, trip.getStatus() != null ? trip.getStatus() : "ACTIVE");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        trip.setTripId(generatedKeys.getInt(1));
                    }
                }
                // Automatically generate physical seats for this new trip
                new SeatDAO().generateSeatsForTrip(conn, trip.getTripId(), trip.getTotalSeats(), trip.getTransportType());
                return true;
            }
        }
        return false;
    }

    /**
     * Toggles a trip's status between ACTIVE and INACTIVE.
     */
    public boolean updateTripStatus(int tripId, String newStatus) throws SQLException {
        String sql = "UPDATE trips SET status = ? WHERE trip_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, tripId);
            return ps.executeUpdate() > 0;
        }
    }

    private Trip mapResultSetToTrip(ResultSet rs) throws SQLException {
        Trip trip = new Trip();
        trip.setTripId(rs.getInt("trip_id"));
        trip.setTransportType(rs.getString("transport_type"));
        trip.setServiceName(rs.getString("service_name"));
        trip.setSource(rs.getString("source"));
        trip.setDestination(rs.getString("destination"));
        trip.setDepartureTime(rs.getString("departure_time"));
        trip.setArrivalTime(rs.getString("arrival_time"));
        trip.setDuration(rs.getString("duration"));
        trip.setFare(rs.getBigDecimal("fare"));
        trip.setTotalSeats(rs.getInt("total_seats"));
        trip.setAvailableSeats(rs.getInt("available_seats"));
        trip.setStatus(rs.getString("status"));
        trip.setCreatedAt(rs.getTimestamp("created_at"));
        return trip;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TravelEase: Live Trips & Schedules (MySQL)     ");
        System.out.println("   Run Date: " + java.time.LocalDate.now() + " (Current Date)");
        System.out.println("==================================================");
        try {
            TripDAO dao = new TripDAO();
            List<Trip> trips = dao.getAllTrips();
            System.out.println(String.format("%-4s | %-6s | %-32s | %-24s | %-18s | %-8s | %-6s",
                    "ID", "Type", "Service Name", "Route", "Timings", "Fare", "Seats"));
            System.out.println("----------------------------------------------------------------------------------------------------------------");
            for (Trip t : trips) {
                System.out.println(String.format("%-4d | %-6s | %-32s | %-24s | %-18s | $%-7.2f | %-6d",
                        t.getTripId(),
                        t.getTransportType(),
                        t.getServiceName().length() > 32 ? t.getServiceName().substring(0, 29) + "..." : t.getServiceName(),
                        t.getSource() + " -> " + t.getDestination(),
                        t.getDepartureTime() + " - " + t.getArrivalTime(),
                        t.getFare(),
                        t.getTotalSeats()));
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------");
            System.out.println("Total Trips in MySQL Database: " + trips.size());
        } catch (Exception e) {
            System.err.println("Error fetching trips: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
