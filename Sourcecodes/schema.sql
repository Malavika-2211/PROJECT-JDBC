-- ============================================================================
-- TravelEase — Bus & Train Reservation System
-- Database Schema Script (MySQL 8.0)
-- ============================================================================
-- IMPORTANT FOR VIVA & EXAMINATIONS:
-- 1. Run this script in MySQL Workbench to create the database and tables.
-- 2. Foreign keys maintain Referential Integrity across relational tables.
-- 3. Dynamic Seat Availability: Seat availability is NOT stored as a static
--    flag; it is dynamically calculated by checking active bookings (CONFIRMED).
--    When a booking is CANCELLED, its seats immediately become available!
-- ============================================================================

CREATE DATABASE IF NOT EXISTS travelease
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE travelease;

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- Stores registered travelers and administrative accounts.
-- Passwords are encrypted using BCrypt hash algorithms before insertion.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_email (email)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. TRIPS TABLE
-- Stores scheduled bus routes and train journeys.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trips (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    transport_type ENUM('BUS', 'TRAIN') NOT NULL,
    service_name VARCHAR(120) NOT NULL,
    source VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time VARCHAR(20) NOT NULL,
    arrival_time VARCHAR(20) NOT NULL,
    duration VARCHAR(30) NOT NULL,
    fare DECIMAL(10, 2) NOT NULL,
    total_seats INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_trips_route (source, destination, status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. SEATS TABLE
-- Stores physical seat designations for each trip.
-- A UNIQUE constraint (trip_id, seat_number) prevents duplicate seat numbers.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS seats (
    seat_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    row_num INT NOT NULL,
    col_letter VARCHAR(5) NOT NULL,
    CONSTRAINT fk_seats_trip
        FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
        ON DELETE CASCADE,
    CONSTRAINT uq_trip_seat
        UNIQUE (trip_id, seat_number),
    INDEX idx_seat_trip (trip_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. BOOKINGS TABLE
-- Stores user reservation transactions.
-- Has a unique booking_reference (e.g. TE-2026-89412).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(30) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    trip_id INT NOT NULL,
    journey_date DATE DEFAULT (CURRENT_DATE),
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'CONFIRMED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_trip
        FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
        ON DELETE RESTRICT,
    INDEX idx_booking_user (user_id),
    INDEX idx_booking_ref (booking_reference),
    INDEX idx_booking_trip_date (trip_id, journey_date, status)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. BOOKING_SEATS TABLE
-- Junction table connecting bookings with individual seats and passenger info.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS booking_seats (
    booking_seat_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender VARCHAR(20) NOT NULL,
    CONSTRAINT fk_bs_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bs_seat
        FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
        ON DELETE RESTRICT,
    INDEX idx_bs_booking (booking_id),
    INDEX idx_bs_seat (seat_id)
) ENGINE=InnoDB;
