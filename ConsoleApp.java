package com.travelease;

import java.util.List;
import java.util.Scanner;

import com.travelease.dao.BookingDAO;
import com.travelease.dao.SeatDAO;
import com.travelease.dao.TripDAO;
import com.travelease.dao.UserDAO;
import com.travelease.model.Booking;
import com.travelease.model.Passenger;
import com.travelease.model.Seat;
import com.travelease.model.Trip;
import com.travelease.model.User;
import com.travelease.util.DBConnection;

/**
 * ConsoleApp - Interactive Terminal Console for College Project Demonstrations
 * 
 * Allows running and inspecting every part of the Java & MySQL backend
 * directly from the VS Code terminal without needing a browser!
 */
public class ConsoleApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("==========================================================");
        System.out.println("   TRAVELEASE: Interactive Console & Database Inspector   ");
        System.out.println("   Session Date: " + java.time.LocalDate.now() + " (Current Date)");
        System.out.println("==========================================================");

        boolean running = true;
        while (running) {
            System.out.println("\nSelect an operation to run:");
            System.out.println("  [1] Output All My Bookings (from MySQL)");
            System.out.println("  [2] Output All Scheduled Trips (Buses & Trains)");
            System.out.println("  [3] Output Seat Layout for a Trip");
            System.out.println("  [4] Test User Authentication (BCrypt Hash Verification)");
            System.out.println("  [5] Test MySQL Database Connection");
            System.out.println("  [0] Exit");
            System.out.print("\nEnter choice (0-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showBookings();
                    break;
                case "2":
                    showTrips();
                    break;
                case "3":
                    showSeats(scanner);
                    break;
                case "4":
                    testAuth();
                    break;
                case "5":
                    testDB();
                    break;
                case "0":
                    running = false;
                    System.out.println("\nExiting Console App. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 0, 1, 2, 3, 4, or 5.");
            }
        }
    }

    private static void showBookings() {
        System.out.println("\n----------------------------------------------------------");
        System.out.println("              OUTPUT: ALL BOOKINGS (FROM MYSQL)           ");
        System.out.println("----------------------------------------------------------");
        try {
            BookingDAO dao = new BookingDAO();
            List<Booking> bookings = dao.getAllBookings();

            if (bookings.isEmpty()) {
                System.out.println("No bookings found in the 'bookings' table.");
                return;
            }

            for (Booking b : bookings) {
                System.out.println("\n[PNR: " + b.getBookingReference() + "] - Status: " + b.getStatus());
                System.out.println("  Service:   " + b.getServiceName() + " (" + b.getTransportType() + ")");
                System.out.println("  Route:     " + b.getSource() + " -> " + b.getDestination());
                System.out.println("  Date:      " + java.time.LocalDate.now() + " (Current Date)");
                System.out.println("  Dep Time:  " + b.getDepartureTime());
                System.out.println("  Arr Time:  " + b.getArrivalTime());
                System.out.println("  Seats:     " + String.join(", ", b.getSeats()));
                System.out.println("  Fare Paid: $" + b.getTotalAmount());
                System.out.println("  Passengers:");
                for (Passenger p : b.getPassengers()) {
                    System.out.println("    * " + p.getName() + " (" + p.getAge() + " yrs, " + p.getGender() + ") [Seat " + p.getSeatNumber() + "]");
                }
            }
            System.out.println("\n----------------------------------------------------------");
            System.out.println("Total Bookings in Database: " + bookings.size());
        } catch (Exception e) {
            System.err.println("Error reading bookings: " + e.getMessage());
        }
    }

    private static void showTrips() {
        System.out.println("\n------------------------------------------------------------------------------------------------");
        System.out.println("                           OUTPUT: SCHEDULED TRIPS (FROM MYSQL)                                 ");
        System.out.println("------------------------------------------------------------------------------------------------");
        try {
            TripDAO dao = new TripDAO();
            List<Trip> trips = dao.getAllTrips();

            System.out.println(String.format("%-4s | %-6s | %-32s | %-24s | %-18s | %-8s",
                    "ID", "Type", "Service Name", "Route", "Timings", "Fare"));
            System.out.println("------------------------------------------------------------------------------------------------");
            for (Trip t : trips) {
                System.out.println(String.format("%-4d | %-6s | %-32s | %-24s | %-18s | $%-7.2f",
                        t.getTripId(),
                        t.getTransportType(),
                        t.getServiceName().length() > 32 ? t.getServiceName().substring(0, 29) + "..." : t.getServiceName(),
                        t.getSource() + " -> " + t.getDestination(),
                        t.getDepartureTime() + " - " + t.getArrivalTime(),
                        t.getFare()));
            }
            System.out.println("------------------------------------------------------------------------------------------------");
            System.out.println("Total Trips in Database: " + trips.size());
        } catch (Exception e) {
            System.err.println("Error reading trips: " + e.getMessage());
        }
    }

    private static void showSeats(Scanner scanner) {
        System.out.print("\nEnter Trip ID to inspect seats (e.g. 1 for Bus, 5 for Train): ");
        String input = scanner.nextLine().trim();
        try {
            int tripId = Integer.parseInt(input);
            SeatDAO dao = new SeatDAO();
            List<Seat> seats = dao.getSeatsForTripAndDate(tripId, java.time.LocalDate.now().toString());

            if (seats.isEmpty()) {
                System.out.println("No seats found for Trip ID " + tripId);
                return;
            }

            System.out.println("\nSeat Status for Trip " + tripId + " (Date: Today):");
            int colCount = 0;
            for (Seat s : seats) {
                String label = String.format("[%3s:%-9s]", s.getSeatNumber(), s.getStatus());
                System.out.print(label + " ");
                colCount++;
                if (colCount % 4 == 0) {
                    System.out.println();
                }
            }
            System.out.println("\nTotal Seats: " + seats.size());
        } catch (Exception e) {
            System.err.println("Error reading seats: " + e.getMessage());
        }
    }

    private static void testAuth() {
        System.out.println("\n----------------------------------------------------------");
        System.out.println("           OUTPUT: BCRYPT AUTHENTICATION TEST             ");
        System.out.println("----------------------------------------------------------");
        try {
            UserDAO dao = new UserDAO();
            User user = dao.authenticate("malavika@example.com", "password123");
            if (user != null) {
                System.out.println("[SUCCESS] Successfully authenticated user:");
                System.out.println("  User ID: " + user.getUserId());
                System.out.println("  Name:    " + user.getName());
                System.out.println("  Email:   " + user.getEmail());
                System.out.println("  Role:    " + user.getRole());
            } else {
                System.out.println("[FAILED] Authentication failed.");
            }
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
        }
    }

    private static void testDB() {
        System.out.println("\n----------------------------------------------------------");
        System.out.println("             OUTPUT: DATABASE CONNECTIVITY TEST           ");
        System.out.println("----------------------------------------------------------");
        DBConnection.testConnection();
    }
}
