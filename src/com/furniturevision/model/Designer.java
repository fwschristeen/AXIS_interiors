package com.furniturevision.model;

import java.io.Serializable;

/**
 * Represents a designer (user) of the AXIS Interiors application.
 * Stores login credentials and profile information.
 */
public class Designer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String role;  // "admin" or "designer"

    public Designer() {}

    public Designer(String id, String username, String passwordHash, String fullName, String email) {
        this(id, username, passwordHash, fullName, email, "designer");
    }

    public Designer(String id, String username, String passwordHash, String fullName, String email, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role : "designer";
    }

    // --- Getters and Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }

    /**
     * Simple password verification using hash comparison.
     */
    public boolean verifyPassword(String password) {
        return this.passwordHash.equals(String.valueOf(password.hashCode()));
    }

    /**
     * Hash a password for storage.
     */
    public static String hashPassword(String password) {
        return String.valueOf(password.hashCode());
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}
