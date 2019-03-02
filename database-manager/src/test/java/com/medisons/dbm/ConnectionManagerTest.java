package com.medisons.dbm;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ConnectionManagerTest {

    private String jdbcUrl = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";

    @Test
    void getConnection_givenValidJdbcUrl_returnValidConnection() {
        try {
            Connection connection = (new ConnectionManager(jdbcUrl)).getConnection();
            assertTrue(connection.isValid(1));
        }
        catch (SQLException e) {
            fail();
        }
    }

    @Test
    void getConnection_givenInvalidJdbcUrl_returnNullConnection() {
        try {
            (new ConnectionManager("invalidJdbcUrl")).getConnection();
            fail();
        } catch (SQLException ignored) { }
    }
}