package com.eversales.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 for hashing with Base64 encoding.
 */
public class PasswordUtil {
    
    /**
     * Hash a plain-text password using SHA-256
     * @param password the plain-text password
     * @return the hashed password (Base64 encoded)
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
    
    /**
     * Verify a plain-text password against a hashed password
     * @param plainPassword the plain-text password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String hashOfPlain = hashPassword(plainPassword);
        return hashOfPlain.equals(hashedPassword);
    }
}