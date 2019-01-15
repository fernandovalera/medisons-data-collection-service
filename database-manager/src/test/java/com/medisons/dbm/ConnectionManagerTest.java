package com.medisons.dbm;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionManagerTest {

    String jdbcUrl = "jdbc:mysql://localhost:3306/";


    @Test
    void getConnection_givenValidJdbcUrl_returnValidConnection() {
        Connection connection = ConnectionManager.getConnection(jdbcUrl);
        try {
            assertTrue(connection.isValid(1));
        }
        catch (SQLException e) {
            fail();
        }
    }

    @Test
    void getConnection_givenInvalidJdbcUrl_returnNullConnection() {
        Connection connection = ConnectionManager.getConnection("invalidJdbcUrl");
        assertNull(connection);
    }
}