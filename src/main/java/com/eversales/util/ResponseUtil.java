package com.eversales.util;

/**
 * Utility class for building standardized JSON API responses.
 * Returns responses in the format: {"success": true/false, "data": {...}, "message": "..."}
 */
public class ResponseUtil {
    
    /**
     * Build a success response with data
     * @param data the data object (as JSON string or already serialized)
     * @return standardized success JSON response
     */
    public static String success(String data) {
        return "{\"success\":true,\"data\":" + data + "}";
    }
    
    /**
     * Build a success response with a simple string value
     * @param key the key for the data
     * @param value the string value
     * @return standardized success JSON response
     */
    public static String success(String key, String value) {
        return "{\"success\":true,\"data\":{\"" + key + "\":\"" + JsonUtil.escapeJson(value) + "\"}}";
    }
    
    /**
     * Build a success response with a numeric value
     * @param key the key for the data
     * @param value the numeric value
     * @return standardized success JSON response
     */
    public static String success(String key, Number value) {
        return "{\"success\":true,\"data\":{\"" + key + "\":" + value + "}}";
    }
    
    /**
     * Build a success response with a boolean value
     * @param key the key for the data
     * @param value the boolean value
     * @return standardized success JSON response
     */
    public static String success(String key, boolean value) {
        return "{\"success\":true,\"data\":{\"" + key + "\":" + value + "}}";
    }
    
    /**
     * Build an error response with message
     * @param message the error message
     * @return standardized error JSON response
     */
    public static String error(String message) {
        return "{\"success\":false,\"message\":\"" + JsonUtil.escapeJson(message) + "\"}";
    }
    
    /**
     * Build an error response with message and data
     * @param message the error message
     * @param data the error data (as JSON string)
     * @return standardized error JSON response with data
     */
    public static String error(String message, String data) {
        return "{\"success\":false,\"message\":\"" + JsonUtil.escapeJson(message) + "\",\"data\":" + data + "}";
    }
    
    /**
     * Build a response indicating unauthorized access
     * @return unauthorized error JSON response
     */
    public static String unauthorized() {
        return error("Unauthorized: Please log in first");
    }
    
    /**
     * Build a response indicating forbidden access
     * @return forbidden error JSON response
     */
    public static String forbidden() {
        return error("Forbidden: You do not have permission to access this resource");
    }
    
    /**
     * Build a response for a not found error
     * @return not found error JSON response
     */
    public static String notFound(String resource) {
        return error(resource + " not found");
    }
}