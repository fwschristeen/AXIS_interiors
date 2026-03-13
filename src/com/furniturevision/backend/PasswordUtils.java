package com.furniturevision.backend;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Provides secure password hashing and verification using SHA-256 with
 * per-user random salts. This replaces the weak String.hashCode() approach.
 * 
 * Security features:
 *  - SHA-256 hashing (industry standard)
 *  - 16-byte cryptographically random salt per user
 *  - Constant-time comparison to prevent timing attacks
 */
public class PasswordUtils {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;  // 128-bit salt

    /**
     * Generates a cryptographically random salt encoded as Base64.
     * Each user gets a unique salt stored alongside their password hash.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with the given salt using SHA-256.
     * The result is returned as a Base64-encoded string for storage.
     * 
     * @param password  the plaintext password
     * @param salt      the Base64-encoded salt
     * @return          the Base64-encoded hash
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Combine salt + password
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            digest.update(saltBytes);
            
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Multiple rounds for added security
            for (int i = 0; i < 1000; i++) {
                digest.reset();
                digest.update(hashBytes);
                digest.update(saltBytes);
                hashBytes = digest.digest();
            }
            
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a password against a stored hash and salt.
     * Uses constant-time comparison to prevent timing attacks.
     * 
     * @param password    the plaintext password to verify
     * @param storedHash  the Base64-encoded stored hash
     * @param storedSalt  the Base64-encoded stored salt
     * @return            true if the password matches
     */
    public static boolean verifyPassword(String password, String storedHash, String storedSalt) {
        String computedHash = hashPassword(password, storedSalt);
        return constantTimeEquals(computedHash, storedHash);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * Always compares all characters regardless of early mismatches.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) return false;

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    /**
     * Validates password strength against policy requirements.
     * Returns null if valid, or an error message if invalid.
     * 
     * Policy:
     *  - Minimum 6 characters
     *  - At least one letter
     *  - At least one digit
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long.";
        }
        
        boolean hasLetter = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasLetter) return "Password must contain at least one letter.";
        if (!hasDigit) return "Password must contain at least one digit.";
        
        return null;  // Valid
    }
}
