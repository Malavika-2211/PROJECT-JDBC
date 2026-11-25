package com.travelease.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String name;
    private String email;
    private transient String passwordHash; // transient so Gson excludes password hash from JSON!
    private String role; // 'USER' or 'ADMIN'
    private Timestamp createdAt;

    public User() {
    }

    public User(int userId, String name, String email, String passwordHash, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("            TravelEase: User Model Test           ");
        System.out.println("==================================================");
        User sample = new User(1, "Malavika", "malavika@example.com", null, "USER");
        System.out.println("Created User instance: " + sample);
        System.out.println("Name: " + sample.getName() + " | Email: " + sample.getEmail() + " | Role: " + sample.getRole());
        System.out.println("[SUCCESS] User model is ready!");
        System.out.println("==================================================");
    }
}
