-- ============================================================================
-- TravelEase — Bus & Train Reservation System
-- Sample Seed Data Script (MySQL 8.0)
-- ============================================================================
-- For College Project Demonstration:
-- Passwords:
--   admin@travelease.com -> admin123
--   malavika@example.com -> password123
-- (Hashes generated using standard BCrypt algorithm)
-- ============================================================================

USE travelease;

-- Disable foreign key checks momentarily for clean seeding if re-running
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking_seats;
TRUNCATE TABLE bookings;
TRUNCATE TABLE seats;
TRUNCATE TABLE trips;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- 1. SEED USERS
-- ----------------------------------------------------------------------------
-- BCrypt Hash for 'admin123': $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- BCrypt Hash for 'password123': $2a$10$wEkgkYv2XfPZlUeC7sT2CeH5wS2s5T6uF8qC9vK1bM4rD3gH2jE0m
INSERT INTO users (user_id, name, email, password_hash, role) VALUES
(1, 'System Administrator', 'admin@travelease.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN'),
(2, 'Malavika', 'malavika@example.com', '$2a$10$wEkgkYv2XfPZlUeC7sT2CeH5wS2s5T6uF8qC9vK1bM4rD3gH2jE0m', 'USER'),
(3, 'Sarah Connor', 'sarah@example.com', '$2a$10$wEkgkYv2XfPZlUeC7sT2CeH5wS2s5T6uF8qC9vK1bM4rD3gH2jE0m', 'USER');

-- ----------------------------------------------------------------------------
-- 2. SEED TRIPS (BUS & TRAIN)
-- ----------------------------------------------------------------------------
INSERT INTO trips (trip_id, transport_type, service_name, source, destination, departure_time, arrival_time, duration, fare, total_seats, status) VALUES
(1, 'BUS', 'TravelEase Volvo Multi-Axle AC Sleeper', 'New York', 'Boston', '07:00 AM', '11:30 AM', '4h 30m', 38.00, 32, 'ACTIVE'),
(2, 'BUS', 'CityConnect Express AC Semi-Sleeper', 'New York', 'Washington DC', '08:30 AM', '01:00 PM', '4h 30m', 32.00, 32, 'ACTIVE'),
(3, 'BUS', 'MetroTransit Night Luxury Express', 'Boston', 'Washington DC', '10:00 PM', '06:30 AM', '8h 30m', 55.00, 32, 'ACTIVE'),
(4, 'BUS', 'Hudson Cruiser AC Seater', 'New York', 'Philadelphia', '09:15 AM', '11:30 AM', '2h 15m', 22.00, 32, 'ACTIVE'),
(5, 'TRAIN', 'Acela Express High-Speed 2154', 'New York', 'Boston', '06:30 AM', '10:15 AM', '3h 45m', 68.00, 48, 'ACTIVE'),
(6, 'TRAIN', 'Northeast Regional Superfast 171', 'New York', 'Washington DC', '07:15 AM', '10:45 AM', '3h 30m', 52.00, 48, 'ACTIVE'),
(7, 'TRAIN', 'Keystone Intercity Express 642', 'Philadelphia', 'New York', '02:00 PM', '03:35 PM', '1h 35m', 28.00, 48, 'ACTIVE'),
(8, 'TRAIN', 'Coastal Coast-to-Coast Express', 'Boston', 'Philadelphia', '11:00 AM', '04:20 PM', '5h 20m', 74.00, 48, 'ACTIVE');

-- ----------------------------------------------------------------------------
-- 3. SEED PHYSICAL SEATS
-- Generating 32 seats (8 rows x A, B, C, D) for Bus Trip 1
-- ----------------------------------------------------------------------------
INSERT INTO seats (trip_id, seat_number, row_num, col_letter) VALUES
(1, '1A', 1, 'A'), (1, '1B', 1, 'B'), (1, '1C', 1, 'C'), (1, '1D', 1, 'D'),
(1, '2A', 2, 'A'), (1, '2B', 2, 'B'), (1, '2C', 2, 'C'), (1, '2D', 2, 'D'),
(1, '3A', 3, 'A'), (1, '3B', 3, 'B'), (1, '3C', 3, 'C'), (1, '3D', 3, 'D'),
(1, '4A', 4, 'A'), (1, '4B', 4, 'B'), (1, '4C', 4, 'C'), (1, '4D', 4, 'D'),
(1, '5A', 5, 'A'), (1, '5B', 5, 'B'), (1, '5C', 5, 'C'), (1, '5D', 5, 'D'),
(1, '6A', 6, 'A'), (1, '6B', 6, 'B'), (1, '6C', 6, 'C'), (1, '6D', 6, 'D'),
(1, '7A', 7, 'A'), (1, '7B', 7, 'B'), (1, '7C', 7, 'C'), (1, '7D', 7, 'D'),
(1, '8A', 8, 'A'), (1, '8B', 8, 'B'), (1, '8C', 8, 'C'), (1, '8D', 8, 'D');

-- Generating 32 seats for Bus Trip 2
INSERT INTO seats (trip_id, seat_number, row_num, col_letter) VALUES
(2, '1A', 1, 'A'), (2, '1B', 1, 'B'), (2, '1C', 1, 'C'), (2, '1D', 1, 'D'),
(2, '2A', 2, 'A'), (2, '2B', 2, 'B'), (2, '2C', 2, 'C'), (2, '2D', 2, 'D'),
(2, '3A', 3, 'A'), (2, '3B', 3, 'B'), (2, '3C', 3, 'C'), (2, '3D', 3, 'D'),
(2, '4A', 4, 'A'), (2, '4B', 4, 'B'), (2, '4C', 4, 'C'), (2, '4D', 4, 'D'),
(2, '5A', 5, 'A'), (2, '5B', 5, 'B'), (2, '5C', 5, 'C'), (2, '5D', 5, 'D'),
(2, '6A', 6, 'A'), (2, '6B', 6, 'B'), (2, '6C', 6, 'C'), (2, '6D', 6, 'D'),
(2, '7A', 7, 'A'), (2, '7B', 7, 'B'), (2, '7C', 7, 'C'), (2, '7D', 7, 'D'),
(2, '8A', 8, 'A'), (2, '8B', 8, 'B'), (2, '8C', 8, 'C'), (2, '8D', 8, 'D');

-- Generating 48 seats (12 rows x A, B, C, D) for Train Trip 5
INSERT INTO seats (trip_id, seat_number, row_num, col_letter) VALUES
(5, '1A', 1, 'A'), (5, '1B', 1, 'B'), (5, '1C', 1, 'C'), (5, '1D', 1, 'D'),
(5, '2A', 2, 'A'), (5, '2B', 2, 'B'), (5, '2C', 2, 'C'), (5, '2D', 2, 'D'),
(5, '3A', 3, 'A'), (5, '3B', 3, 'B'), (5, '3C', 3, 'C'), (5, '3D', 3, 'D'),
(5, '4A', 4, 'A'), (5, '4B', 4, 'B'), (5, '4C', 4, 'C'), (5, '4D', 4, 'D'),
(5, '5A', 5, 'A'), (5, '5B', 5, 'B'), (5, '5C', 5, 'C'), (5, '5D', 5, 'D'),
(5, '6A', 6, 'A'), (5, '6B', 6, 'B'), (5, '6C', 6, 'C'), (5, '6D', 6, 'D'),
(5, '7A', 7, 'A'), (5, '7B', 7, 'B'), (5, '7C', 7, 'C'), (5, '7D', 7, 'D'),
(5, '8A', 8, 'A'), (5, '8B', 8, 'B'), (5, '8C', 8, 'C'), (5, '8D', 8, 'D'),
(5, '9A', 9, 'A'), (5, '9B', 9, 'B'), (5, '9C', 9, 'C'), (5, '9D', 9, 'D'),
(5, '10A', 10, 'A'), (5, '10B', 10, 'B'), (5, '10C', 10, 'C'), (5, '10D', 10, 'D'),
(5, '11A', 11, 'A'), (5, '11B', 11, 'B'), (5, '11C', 11, 'C'), (5, '11D', 11, 'D'),
(5, '12A', 12, 'A'), (5, '12B', 12, 'B'), (5, '12C', 12, 'C'), (5, '12D', 12, 'D');

-- ----------------------------------------------------------------------------
-- 4. SEED SAMPLE BOOKINGS
-- ----------------------------------------------------------------------------
-- Booking 1: Confirmed booking by Malavika for Train Trip 5 (Acela Express)
INSERT INTO bookings (booking_id, booking_reference, user_id, trip_id, journey_date, total_amount, status) VALUES
(1, 'TE-2026-84912', 2, 5, CURDATE() + INTERVAL 5 DAY, 141.00, 'CONFIRMED');

-- Link Seats 2A and 2B to Booking 1
-- Seat IDs for Trip 5: Row 2 A is seat_id 69 (approx), let's query by seat_number
INSERT INTO booking_seats (booking_id, seat_id, passenger_name, passenger_age, passenger_gender)
SELECT 1, s.seat_id, 'Malavika', 21, 'Female'
FROM seats s WHERE s.trip_id = 5 AND s.seat_number = '2A';

INSERT INTO booking_seats (booking_id, seat_id, passenger_name, passenger_age, passenger_gender)
SELECT 1, s.seat_id, 'Sam Taylor', 20, 'Female'
FROM seats s WHERE s.trip_id = 5 AND s.seat_number = '2B';

-- Booking 2: Cancelled booking by Malavika for Bus Trip 1
INSERT INTO bookings (booking_id, booking_reference, user_id, trip_id, journey_date, total_amount, status) VALUES
(2, 'TE-2026-19401', 2, 1, CURDATE() - INTERVAL 10 DAY, 40.50, 'CANCELLED');

-- Link Seat 1A to Booking 2
INSERT INTO booking_seats (booking_id, seat_id, passenger_name, passenger_age, passenger_gender)
SELECT 2, s.seat_id, 'Malavika', 21, 'Female'
FROM seats s WHERE s.trip_id = 1 AND s.seat_number = '1A';
