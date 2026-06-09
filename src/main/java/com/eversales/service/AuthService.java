package com.eversales.service;

import com.eversales.model.User;
import com.eversales.repository.UserDAO;
import com.eversales.util.PasswordUtil;

/**
 * Service layer for authentication operations.
 * Handles user registration, login, and password verification.
 */
public class AuthService {
    private UserDAO userDAO = new UserDAO();
    
    /**
     * Register a new user
     * @return the User object with ID if successful, null otherwise
     */
    public User register(String fullName, String email, String plainPassword, String phoneNumber) {
        // Check if email already exists
        User existing = userDAO.getUserByEmail(email);
        if (existing != null) {
            System.err.println("Email already exists: " + email);
            return null;
        }
        
        // Hash the password
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        
        // Create new user
        User newUser = new User(fullName, email, hashedPassword, "customer", phoneNumber);
        boolean success = userDAO.createUser(newUser);
        
        if (success) {
            // Fetch and return the created user (with ID)
            return userDAO.getUserByEmail(email);
        }
        return null;
    }
    
    /**
     * Login a user
     * @return the User object if credentials are correct, null otherwise
     */
    public User login(String email, String plainPassword) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            System.err.println("User not found: " + email);
            return null;
        }
        
        // Verify password
        if (PasswordUtil.verifyPassword(plainPassword, user.getPassword())) {
            return user;
        }
        
        System.err.println("Incorrect password for user: " + email);
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    /**
     * Update user profile
     */
    public boolean updateUserProfile(int userId, String fullName, String phoneNumber) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        return userDAO.updateUser(user);
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // Verify old password
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            System.err.println("Old password is incorrect");
            return false;
        }
        
        // Hash and update new password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        user.setPassword(hashedPassword);
        return userDAO.updateUser(user);
    }
}
