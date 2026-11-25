package com.travelease.dao;

import com.travelease.model.User;
import com.travelease.util.DBConnection;
import com.travelease.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO - Data Access Object for User Management & Authentication
 * 
 * Demonstrates:
 * 1. BCrypt password verification.
 * 2. PreparedStatement parameterized credentials lookup.
 * 3. Prevention of duplicate email accounts.
 */
public class UserDAO {

    /**
     * Registers a new user with a hashed password.
     */
    public User register(String name, String email, String plainPassword, String role) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, hashedPassword);
            ps.setString(4, role != null ? role.toUpperCase() : "USER");

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int userId = rs.getInt(1);
                        return new User(userId, name, email, null, role);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Authenticates a user by email and plain-text candidate password.
     */
    public User authenticate(String email, String plainPassword) throws SQLException {
        String sql = "SELECT user_id, name, email, password_hash, role, created_at FROM users WHERE LOWER(email) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.checkPassword(plainPassword, storedHash)) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setName(rs.getString("name"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        }
        return null; // Authentication failed
    }

    /**
     * Checks if an email address is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves user profile by ID.
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, role, created_at FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   TravelEase: User Authentication Test (MySQL)   ");
        System.out.println("==================================================");
        try {
            UserDAO dao = new UserDAO();

            System.out.println("1. Testing Admin Login ('admin@travelease.com' / 'password123')...");
            User admin = dao.authenticate("admin@travelease.com", "password123");
            if (admin != null) {
                System.out.println("   [SUCCESS] Logged in as: " + admin.getName() + " (Role: " + admin.getRole() + ")");
            } else {
                System.out.println("   [FAILED] Could not authenticate admin.");
            }

            System.out.println("\n2. Testing Customer Login ('malavika@example.com' / 'password123')...");
            User user = dao.authenticate("malavika@example.com", "password123");
            if (user != null) {
                System.out.println("   [SUCCESS] Logged in as: " + user.getName() + " (Email: " + user.getEmail() + ")");
            } else {
                System.out.println("   [FAILED] Could not authenticate user.");
            }

            System.out.println("\n3. Testing Wrong Password ('malavika@example.com' / 'wrongpassword')...");
            User invalid = dao.authenticate("malavika@example.com", "wrongpassword");
            if (invalid == null) {
                System.out.println("   [PASSED] Authentication correctly rejected invalid password!");
            } else {
                System.out.println("   [SECURITY FLAW] Incorrect password accepted!");
            }
            System.out.println("==================================================");
        } catch (Exception e) {
            System.err.println("Error testing user auth: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
