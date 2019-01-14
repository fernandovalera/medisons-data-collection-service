package com.medisons.dbm;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ConnectionManagerTest {

    @Container
    static private JdbcDatabaseContainer mysql = new MySQLContainer()
            .withUsername("root")
            .withPassword("")
            .withDatabaseName("signals")
            .withInitScript("db_setup.sql");

    @Test
    void getConnection_givenValidJdbcUrl_returnValidConnection() {
        Connection connection = ConnectionManager.getConnection(mysql.getJdbcUrl());
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