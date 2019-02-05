package com.medisons.dbm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class.getName());

    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    static Connection getConnection(String url) throws SQLException {
        Connection conn;

        try {
            Properties connectionProperties = new Properties();
            connectionProperties.put("serverTimezone", "UTC");
            connectionProperties.put("user", USERNAME);
            connectionProperties.put("password", PASSWORD);

            conn = DriverManager.getConnection(url, connectionProperties);
        } catch (SQLException e) {
            LOG.warn("Failed to create the database connection.");
            throw new SQLException(e);
        }

        return conn;
    }
}