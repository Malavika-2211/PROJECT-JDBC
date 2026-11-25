package com.travelease.dao;

import com.travelease.model.Booking;
import com.travelease.model.Passenger;
import com.travelease.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * BookingDAO - Transactional Reservation Operations
 * 
 * CRITICAL VIVA CONCEPTS:
 * 1. Transactions: Multiple SQL statements execute as a single atomic unit.
 * 2. conn.setAutoCommit(false): Disables MySQL's default auto-commit after every statement.
 * 3. Concurrency Protection: Re-checks and locks selected seats with 'FOR UPDATE' or availability
 *    checks before inserting to prevent race conditions where two users book the same seat at the same second.
 * 4. conn.commit(): Commits changes permanently to the database if all inserts succeed.
 * 5. conn.rollback(): Undoes ALL inserts if any check, constraint, or network failure happens,
 *    ensuring the database never contains partial or orphaned records.
 */
public class BookingDAO {

    /**
     * Creates a new reservation atomically using a database transaction.
     */
    public Booking createBooking(int userId, int tripId, String journeyDate, 
                                 List<String> seatNumbers, List<Passenger> passengers) throws SQLException {
        if (journeyDate == null || journeyDate.trim().isEmpty()) {
            journeyDate = java.time.LocalDate.now().toString();
        }

        if (seatNumbers == null || seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // STEP 1: Begin Database Transaction
            conn.setAutoCommit(false);

            // STEP 2: Fetch and verify official trip details and authoritative fare
            BigDecimal officialFare;
            String serviceName, transportType, source, dest, depTime, arrTime;
            String tripSql = "SELECT service_name, transport_type, source, destination, " +
                             "departure_time, arrival_time, fare, status FROM trips WHERE trip_id = ?";
            try (PreparedStatement psTrip = conn.prepareStatement(tripSql)) {
                psTrip.setInt(1, tripId);
                try (ResultSet rs = psTrip.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Trip ID " + tripId + " not found.");
                    }
                    if (!"ACTIVE".equalsIgnoreCase(rs.getString("status"))) {
                        throw new IllegalStateException("Trip is currently inactive for reservations.");
                    }
                    serviceName = rs.getString("service_name");
                    transportType = rs.getString("transport_type");
                    source = rs.getString("source");
                    dest = rs.getString("destination");
                    depTime = rs.getString("departure_time");
                    arrTime = rs.getString("arrival_time");
                    officialFare = rs.getBigDecimal("fare");
                }
            }

            // STEP 3: Map Seat Numbers to Seat IDs for this Trip
            Map<String, Integer> seatNumberToId = new HashMap<>();
            String seatsSql = "SELECT seat_id, seat_number FROM seats WHERE trip_id = ?";
            try (PreparedStatement psSeats = conn.prepareStatement(seatsSql)) {
                psSeats.setInt(1, tripId);
                try (ResultSet rs = psSeats.executeQuery()) {
                    while (rs.next()) {
                        seatNumberToId.put(rs.getString("seat_number"), rs.getInt("seat_id"));
                    }
                }
            }

            List<Integer> seatIds = new ArrayList<>();
            for (String seatNum : seatNumbers) {
                Integer seatId = seatNumberToId.get(seatNum);
                if (seatId == null) {
                    throw new IllegalArgumentException("Invalid seat designation: " + seatNum);
                }
                seatIds.add(seatId);
            }

            // STEP 4: Concurrency Check — Verify no seat is already booked for this date
            // Using parameterized IN clause
            StringBuilder checkSql = new StringBuilder(
                "SELECT s.seat_number FROM booking_seats bs " +
                "JOIN bookings b ON bs.booking_id = b.booking_id " +
                "JOIN seats s ON bs.seat_id = s.seat_id " +
                "WHERE b.trip_id = ? AND b.journey_date = ? AND b.status = 'CONFIRMED' AND bs.seat_id IN ("
            );
            for (int i = 0; i < seatIds.size(); i++) {
                checkSql.append(i == 0 ? "?" : ", ?");
            }
            checkSql.append(") FOR UPDATE");

            try (PreparedStatement psCheck = conn.prepareStatement(checkSql.toString())) {
                psCheck.setInt(1, tripId);
                psCheck.setString(2, journeyDate);
                for (int i = 0; i < seatIds.size(); i++) {
                    psCheck.setInt(3 + i, seatIds.get(i));
                }

                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        String conflictingSeat = rs.getString("seat_number");
                        throw new IllegalStateException("Seat " + conflictingSeat + " is already booked for this journey date!");
                    }
                }
            }

            // STEP 5: Calculate authoritative total amount on the backend
            BigDecimal feePerSeat = new BigDecimal("2.50");
            BigDecimal seatCount = new BigDecimal(seatNumbers.size());
            BigDecimal totalAmount = officialFare.multiply(seatCount).add(feePerSeat.multiply(seatCount));

            // Generate unique PNR (e.g. TE-2026-78192)
            String bookingReference = "TE-" + java.time.Year.now().getValue() + "-" + (10000 + new Random().nextInt(90000));

            // STEP 6: Insert into 'bookings' table
            int bookingId;
            String insertBookingSql = "INSERT INTO bookings (booking_reference, user_id, trip_id, journey_date, " +
                                      "total_amount, status) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";
            try (PreparedStatement psBooking = conn.prepareStatement(insertBookingSql, Statement.RETURN_GENERATED_KEYS)) {
                psBooking.setString(1, bookingReference);
                psBooking.setInt(2, userId);
                psBooking.setInt(3, tripId);
                psBooking.setString(4, journeyDate);
                psBooking.setBigDecimal(5, totalAmount);

                psBooking.executeUpdate();
                try (ResultSet generatedKeys = psBooking.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        bookingId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated booking ID.");
                    }
                }
            }

            // STEP 7: Insert into 'booking_seats' table
            String insertBsSql = "INSERT INTO booking_seats (booking_id, seat_id, passenger_name, " +
                                 "passenger_age, passenger_gender) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psBs = conn.prepareStatement(insertBsSql)) {
                for (int i = 0; i < seatNumbers.size(); i++) {
                    String seatNum = seatNumbers.get(i);
                    int sId = seatNumberToId.get(seatNum);

                    Passenger p = (passengers != null && i < passengers.size()) 
                                    ? passengers.get(i) 
                                    : new Passenger("Passenger " + (i + 1), 25, "Male", seatNum);

                    psBs.setInt(1, bookingId);
                    psBs.setInt(2, sId);
                    psBs.setString(3, p.getName());
                    psBs.setInt(4, p.getAge());
                    psBs.setString(5, p.getGender());
                    psBs.addBatch();
                }
                psBs.executeBatch();
            }

            // STEP 8: All steps succeeded — Commit Transaction!
            conn.commit();

            // Construct return object
            Booking booking = new Booking();
            booking.setBookingId(bookingId);
            booking.setBookingReference(bookingReference);
            booking.setUserId(userId);
            booking.setTripId(tripId);
            booking.setJourneyDate(java.sql.Date.valueOf(journeyDate));
            booking.setTotalAmount(totalAmount);
            booking.setStatus("CONFIRMED");
            booking.setServiceName(serviceName);
            booking.setTransportType(transportType);
            booking.setSource(source);
            booking.setDestination(dest);
            booking.setDepartureTime(depTime);
            booking.setArrivalTime(arrTime);
            booking.setSeats(seatNumbers);
            booking.setPassengers(passengers);

            return booking;

        } catch (SQLException | RuntimeException e) {
            // STEP 9: Rollback on any failure
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[BookingDAO] Transaction rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Cancels an eligible booking, immediately releasing its reserved seats.
     */
    public boolean cancelBooking(String bookingReference, int userId, boolean isAdmin) throws SQLException {
        String verifySql = "SELECT user_id, status FROM bookings WHERE booking_reference = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psVerify = conn.prepareStatement(verifySql)) {

            psVerify.setString(1, bookingReference);
            try (ResultSet rs = psVerify.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                int ownerId = rs.getInt("user_id");
                String currentStatus = rs.getString("status");

                // Check authorization
                if (!isAdmin && ownerId != userId) {
                    throw new SecurityException("Unauthorized: You do not own this reservation.");
                }

                if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                    return true; // Already cancelled
                }
            }

            // Update status to CANCELLED
            String updateSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_reference = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, bookingReference);
                return psUpdate.executeUpdate() > 0;
            }
        }
    }

    /**
     * Retrieves all bookings for a specific user (upcoming, completed, and cancelled).
     */
    public List<Booking> getUserBookings(int userId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, t.service_name, t.transport_type, t.source, t.destination, " +
                     "       t.departure_time, t.arrival_time " +
                     "FROM bookings b " +
                     "JOIN trips t ON b.trip_id = t.trip_id " +
                     "WHERE b.user_id = ? " +
                     "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = mapBookingDetails(conn, rs);
                    bookings.add(b);
                }
            }
        }
        return bookings;
    }

    /**
     * Retrieves all customer bookings across the system for the Admin dashboard.
     */
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, t.service_name, t.transport_type, t.source, t.destination, " +
                     "       t.departure_time, t.arrival_time " +
                     "FROM bookings b " +
                     "JOIN trips t ON b.trip_id = t.trip_id " +
                     "ORDER BY b.booking_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                bookings.add(mapBookingDetails(conn, rs));
            }
        }
        return bookings;
    }

    private Booking mapBookingDetails(Connection conn, ResultSet rs) throws SQLException {
        Booking b = new Booking();
        int bookingId = rs.getInt("booking_id");
        b.setBookingId(bookingId);
        b.setBookingReference(rs.getString("booking_reference"));
        b.setUserId(rs.getInt("user_id"));
        b.setTripId(rs.getInt("trip_id"));
        b.setJourneyDate(rs.getDate("journey_date"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setStatus(rs.getString("status"));
        b.setBookingDate(rs.getTimestamp("booking_date"));
        b.setServiceName(rs.getString("service_name"));
        b.setTransportType(rs.getString("transport_type"));
        b.setSource(rs.getString("source"));
        b.setDestination(rs.getString("destination"));
        b.setDepartureTime(rs.getString("departure_time"));
        b.setArrivalTime(rs.getString("arrival_time"));

        // Fetch associated seats and passengers
        String bsSql = "SELECT s.seat_number, bs.passenger_name, bs.passenger_age, bs.passenger_gender " +
                       "FROM booking_seats bs " +
                       "JOIN seats s ON bs.seat_id = s.seat_id " +
                       "WHERE bs.booking_id = ?";
        try (PreparedStatement psBs = conn.prepareStatement(bsSql)) {
            psBs.setInt(1, bookingId);
            try (ResultSet rsBs = psBs.executeQuery()) {
                List<String> seatList = new ArrayList<>();
                List<Passenger> passengerList = new ArrayList<>();
                while (rsBs.next()) {
                    String seatNum = rsBs.getString("seat_number");
                    seatList.add(seatNum);
                    passengerList.add(new Passenger(
                        rsBs.getString("passenger_name"),
                        rsBs.getInt("passenger_age"),
                        rsBs.getString("passenger_gender"),
                        seatNum
                    ));
                }
                b.setSeats(seatList);
                b.setPassengers(passengerList);
            }
        }
        return b;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TravelEase: Live Bookings Output (MySQL)       ");
        System.out.println("==================================================");
        try {
            BookingDAO dao = new BookingDAO();
            List<Booking> bookings = dao.getAllBookings();
            if (bookings.isEmpty()) {
                System.out.println("No bookings currently recorded in the database.");
            } else {
                for (Booking b : bookings) {
                    System.out.println("--------------------------------------------------");
                    System.out.println("PNR Reference : " + b.getBookingReference());
                    System.out.println("Service       : " + b.getServiceName() + " (" + b.getTransportType() + ")");
                    System.out.println("Route         : " + b.getSource() + " -> " + b.getDestination());
                    System.out.println("Date (Current): " + java.time.LocalDate.now());
                    System.out.println("Dep Time      : " + b.getDepartureTime());
                    System.out.println("Arr Time      : " + b.getArrivalTime());
                    System.out.println("Seats Reserved: " + String.join(", ", b.getSeats()));
                    System.out.println("Total Amount  : $" + b.getTotalAmount());
                    System.out.println("Status        : " + b.getStatus());
                    System.out.println("Passengers    :");
                    for (Passenger p : b.getPassengers()) {
                        System.out.println("  * " + p.getName() + " (" + p.getAge() + " yrs, " + p.getGender() + ") [Seat " + p.getSeatNumber() + "]");
                    }
                }
                System.out.println("--------------------------------------------------");
                System.out.println("Total Bookings Found: " + bookings.size());
            }
        } catch (Exception e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
