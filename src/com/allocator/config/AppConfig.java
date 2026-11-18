package com.allocator.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Application Configuration Loader for MySQL JDBC Connectivity and Scheduling Parameters.
 * Supports reading from db.properties or environment variables.
 */
public class AppConfig {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB = "conflict_free_allocator";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;

    public static final String[] STANDARD_TIME_SLOTS = {
        "Slot 1: Mon 09:00 AM - 12:00 PM",
        "Slot 2: Mon 02:00 PM - 05:00 PM",
        "Slot 3: Tue 09:00 AM - 12:00 PM",
        "Slot 4: Tue 02:00 PM - 05:00 PM",
        "Slot 5: Wed 09:00 AM - 12:00 PM",
        "Slot 6: Wed 02:00 PM - 05:00 PM",
        "Slot 7: Thu 09:00 AM - 12:00 PM",
        "Slot 8: Thu 02:00 PM - 05:00 PM",
        "Slot 9: Fri 09:00 AM - 12:00 PM",
        "Slot 10: Fri 02:00 PM - 05:00 PM"
    };

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException ignored) {
            // Fall back to environment variables or defaults if db.properties is not in working dir
        }

        String host = getValue(props, "db.host", "DB_HOST", DEFAULT_HOST);
        String port = getValue(props, "db.port", "DB_PORT", DEFAULT_PORT);
        String dbName = getValue(props, "db.name", "DB_NAME", DEFAULT_DB);
        dbUser = getValue(props, "db.user", "DB_USER", DEFAULT_USER);
        dbPassword = getValue(props, "db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

        jdbcUrl = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            host, port, dbName
        );
    }

    private static String getValue(Properties props, String propKey, String envKey, String defaultVal) {
        if (props.containsKey(propKey) && !props.getProperty(propKey).trim().isEmpty()) {
            return props.getProperty(propKey).trim();
        }
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        return defaultVal;
    }

    public static String getJdbcUrl() {
        return jdbcUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void updateConnectionCredentials(String url, String user, String password) {
        jdbcUrl = url;
        dbUser = user;
        dbPassword = password;
    }

    public static String getTimeSlotLabel(int colorIndex) {
        if (colorIndex >= 0 && colorIndex < STANDARD_TIME_SLOTS.length) {
            return STANDARD_TIME_SLOTS[colorIndex];
        }
        return "Slot " + (colorIndex + 1) + " (Extended Schedule)";
    }
}
