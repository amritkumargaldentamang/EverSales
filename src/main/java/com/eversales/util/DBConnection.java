package com.eversales.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection{
    private static final String URL = "jdbc:mysql://localhost:3306/eversales";
    private static final String USER = "root";
    private static final String PASSWORD = ""; //Set my xammp password here

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } 
        
        catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database.", e);
        }
    }
}