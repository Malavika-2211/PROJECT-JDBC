package com.travelease.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.travelease.model.Seat;
import com.travelease.util.DBConnection;

/**
 * SeatDAO - Data Access Object for Vehicle Physical Seats
 * 
 * Demonstrates:
 * 1. Dynamic calculation of seat status per journey date.
 * 2. Automatic seat layout generation when new trips are scheduled.
 */
public class SeatDAO {

    /**
     * Retrieves all seats for a specific trip, calculating whether each seat is
     * 'AVAILABLE' or 'BOOKED' on the requested journey date.
     */
    public List<Seat> getSeatsForTripAndDate(int tripId, String journeyDate) throws SQLException {
        List<Seat> seats = new ArrayList<>();
        boolean hasDate = journeyDate != null && !journeyDate.trim().isEmpty();

        String sql = "SELECT s.seat_id, s.trip_id, s.seat_number, s.row_num, s.col_letter, " +
                     "  CASE WHEN active_booking.booking_seat_id IS NOT NULL THEN 'BOOKED' " +
                     "       ELSE 'AVAILABLE' END AS status " +
                     "FROM seats s " +
                     "LEFT JOIN ( " +
                     "  SELECT bs.seat_id, bs.booking_seat_id " +
                     "  FROM booking_seats bs " +
                     "  JOIN bookings b ON bs.booking_id = b.booking_id " +
                     "  WHERE b.trip_id = ? " + (hasDate ? "AND b.journey_date = ? " : "") + "AND b.status = 'CONFIRMED' " +
                     ") active_booking ON s.seat_id = active_booking.seat_id " +
                     "WHERE s.trip_id = ? " +
                     "ORDER BY s.row_num ASC, s.col_letter ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, tripId);
            if (hasDate) {
                ps.setString(idx++, journeyDate);
            }
            ps.setInt(idx++, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setSeatId(rs.getInt("seat_id"));
                    seat.setTripId(rs.getInt("trip_id"));
                    seat.setSeatNumber(rs.getString("seat_number"));
                    seat.setRowNum(rs.getInt("row_num"));
                    seat.setColLetter(rs.getString("col_letter"));
                    seat.setStatus(rs.getString("status"));
                    seats.add(seat);
                }
            }
        }
        return seats;
    }

    /**
     * Helper to generate standard 2x2 physical seats (Rows 1..N x A, B, C, D) 
     * for newly created trips.
     */
    public void generateSeatsForTrip(Connection conn, int tripId, int totalSeats, String transportType) throws SQLException {
        String sql = "INSERT INTO seats (trip_id, seat_number, row_num, col_letter) VALUES (?, ?, ?, ?)";
        String[] columns = {"A", "B", "C", "D"};
        int totalRows = totalSeats / 4;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int r = 1; r <= totalRows; r++) {
                for (String c : columns) {
                    ps.setInt(1, tripId);
                    ps.setString(2, r + c);
                    ps.setInt(3, r);
                    ps.setString(4, c);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TravelEase: Live Seat Matrix Output (MySQL)    ");
        System.out.println("   Run Date: " + java.time.LocalDate.now());
        System.out.println("==================================================");
        try {
            SeatDAO dao = new SeatDAO();
            int tripId = 1;
            String today = java.time.LocalDate.now().toString();
            List<Seat> seats = dao.getSeatsForTripAndDate(tripId, today);
            System.out.println("Displaying seats for Trip ID: " + tripId + " (Journey Date: " + today + ")");
            System.out.println("--------------------------------------------------");
            System.out.println(String.format("%-8s | %-6s | %-6s | %-12s", "Seat No", "Row", "Col", "Status"));
            System.out.println("--------------------------------------------------");
            for (Seat s : seats) {
                System.out.println(String.format("%-8s | %-6d | %-6s | %-12s", 
                    s.getSeatNumber(), s.getRowNum(), s.getColLetter(), s.getStatus()));
            }
            System.out.println("--------------------------------------------------");
            System.out.println("Total Seats in Layout: " + seats.size());
        } catch (Exception e) {
            System.err.println("Error fetching seats: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
