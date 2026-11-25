package com.travelease.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * PasswordUtil - Secure Hashing Utility using BCrypt
 * 
 * 1. Plaintext Passwords should NEVER be stored in databases. If a database is breached,
 *    plaintext passwords expose users across multiple services.
 * 2. BCrypt incorporates a random cryptographic "salt" and an adaptive work factor (log rounds).
 *    Even if two users have the same password ("password123"), their resulting hashes will be
 *    completely different.
 * 3. BCrypt is one-way: you cannot decrypt the hash back to the original password. Instead,
 *    BCrypt.checkpw() re-hashes the incoming user attempt with the existing salt and compares the hashes.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 10;

    /**
     * Hashes a plain-text password using BCrypt with a secure random salt.
     * 
     * @param plainTextPassword the raw password entered by the user
     * @return 60-character BCrypt hash string safe for database storage
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies whether a candidate plain-text password matches an existing stored hash.
     * 
     * @param plainTextPassword candidate password entered during login
     * @param storedHash hash retrieved from the database users table
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String storedHash) {
        if (plainTextPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, storedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("[PasswordUtil] Invalid BCrypt hash format: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      TravelEase: BCrypt Password Hashing Test     ");
        System.out.println("==================================================");
        String rawPassword = "password123";
        System.out.println("Input Password: " + rawPassword);
        
        String hashed1 = hashPassword(rawPassword);
        String hashed2 = hashPassword(rawPassword);
        System.out.println("Generated Hash 1: " + hashed1);
        System.out.println("Generated Hash 2: " + hashed2);
        System.out.println("Note: Salt makes every hash unique even for identical input!");
        
        boolean match1 = checkPassword("password123", hashed1);
        boolean matchWrong = checkPassword("wrongPassword!", hashed1);
        System.out.println("Verification ('password123'):    " + (match1 ? "[PASSED]" : "[FAILED]"));
        System.out.println("Verification ('wrongPassword!'): " + (!matchWrong ? "[CORRECTLY REJECTED]" : "[FAILED]"));
        System.out.println("==================================================");
    }
}
