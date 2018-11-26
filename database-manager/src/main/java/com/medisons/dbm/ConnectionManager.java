package com.medisons.dbm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class ConnectionManager {

    private static final Logger LOG = Logger.getLogger(ConnectionManager.class.getName());

    private static final String URL = "jdbc:mysql://localhost:3306/signals";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static Connection conn;

    public static Connection getConnection() {
        try {
            Properties connectionProperties = new Properties();
            connectionProperties.put("serverTimezone", "UTC");
            connectionProperties.put("user", USERNAME);
            connectionProperties.put("password", PASSWORD);

            conn = DriverManager.getConnection(URL, connectionProperties);
        } catch (SQLException e) {
            LOG.warning("Failed to create the database connection.");
        }
        return conn;
    }
}