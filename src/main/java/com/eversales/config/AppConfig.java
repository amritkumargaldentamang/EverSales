package com.eversales.config;

/**
 * Application-wide configuration and constants.
 */
public class AppConfig {
    
    // Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/eversales";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Update with your XAMPP password
    
    // Application Constants
    public static final String APP_NAME = "EverSales";
    public static final String APP_VERSION = "0.0.1";
    
    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_SELLER = "seller";
    
    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_COMPLETED = "completed";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    
    // Payment Status
    public static final String PAYMENT_STATUS_PENDING = "pending";
    public static final String PAYMENT_STATUS_COMPLETED = "completed";
    public static final String PAYMENT_STATUS_FAILED = "failed";
    
    // Payment Methods
    public static final String PAYMENT_METHOD_CREDIT_CARD = "credit_card";
    public static final String PAYMENT_METHOD_PAYPAL = "paypal";
    public static final String PAYMENT_METHOD_BANK_TRANSFER = "bank_transfer";
    
    // Session Keys
    public static final String SESSION_KEY_USER = "user";
    public static final String SESSION_KEY_USER_ID = "userId";
    public static final String SESSION_KEY_CART = "cart";
    
    // API Response Constants
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_DATA = "data";
    public static final String RESPONSE_MESSAGE = "message";
}
