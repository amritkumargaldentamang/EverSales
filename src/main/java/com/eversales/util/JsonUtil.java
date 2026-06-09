package com.eversales.util;

/**
 * Utility class for JSON serialization and deserialization.
 * Provides simple JSON building methods without external dependencies.
 */
public class JsonUtil {
    
    /**
     * Escape special characters in a string for JSON
     * @param value the string to escape
     * @return the escaped string
     */
    public static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch < 32) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }
    
    /**
     * Build a simple JSON object string
     * @param key the key
     * @param value the value (as string)
     * @return JSON string: {"key": "value"}
     */
    public static String toJson(String key, String value) {
        return "{\"" + key + "\":\"" + escapeJson(value) + "\"}";
    }
    
    /**
     * Build a simple JSON object string with numeric value
     * @param key the key
     * @param value the numeric value
     * @return JSON string: {"key": value}
     */
    public static String toJson(String key, Number value) {
        return "{\"" + key + "\":" + value + "}";
    }
    
    /**
     * Build a simple JSON object string with boolean value
     * @param key the key
     * @param value the boolean value
     * @return JSON string: {"key": true/false}
     */
    public static String toJson(String key, boolean value) {
        return "{\"" + key + "\":" + value + "}";
    }
}