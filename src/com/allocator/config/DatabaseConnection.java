package com.allocator.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Strict MySQL JDBC Connection Manager.
 * Loads the MySQL Connector/J driver and provides transactional Connections.
 */
public class DatabaseConnection {

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath.");
            System.err.println("Ensure mysql-connector-j-8.x.jar is included when running the application.");
        }
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Opens and returns a live JDBC Connection to the configured MySQL database.
     *
     * @return valid java.sql.Connection
     * @throws SQLException if MySQL server is unreachable or credentials are invalid
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            AppConfig.getJdbcUrl(),
            AppConfig.getDbUser(),
            AppConfig.getDbPassword()
        );
    }

    /**
     * Tests whether the MySQL database connection is active and valid.
     *
     * @return true if connection succeeds within 3 seconds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
