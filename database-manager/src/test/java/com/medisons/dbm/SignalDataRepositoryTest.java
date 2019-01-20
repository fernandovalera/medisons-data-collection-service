package com.medisons.dbm;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignalDataRepositoryTest {

    private static final String SPO2_NAME = "spo2";
    private static final double SPO2_FREQUENCY = 1.0d;

    private static final String SPO2_TIMESTAMP_STRING = "2019.01.01 00:00:00.000";
    private static final long SPO2_TIMESTAMP_1 = 1546300800000L;
    private static final long SPO2_TIMESTAMP_2 = 1546300801000L;
    private static final long SPO2_TIMESTAMP_3 = 1546300802000L;

    private static final double SPO2_VALUE_1 = 1.5D;
    private static final double SPO2_VALUE_2 = 2.7D;
    private static final double SPO2_VALUE_3 = 3.4D;

    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB = "test_signals";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Flyway flyway;
    private static Connection connection;

    private SignalDataRepository signalDataRepository;

    @BeforeAll
    static void setUpConnection () throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);

        try {
            connection.prepareStatement(String.format("CREATE DATABASE %s", DB)).executeUpdate();
        } catch (SQLException e) {
            // Database already exists.
        }

        connection.prepareStatement(String.format("USE %s", DB)).executeUpdate();
        flyway = Flyway.configure().dataSource(URL + DB, USER, PASSWORD).load();
    }

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();

        signalDataRepository = new SignalDataRepository(connection);
    }

    @AfterEach
    void tearDown() {
        signalDataRepository = null;
    }

    @Test
    void getAllSignalData_givenSPO2DataQuery_returnTwoItems() throws SQLException {
        PreparedStatement preparedStatement1 = connection.prepareStatement("INSERT INTO signal_info VALUES (?, ?)");
        preparedStatement1.setString(1, SPO2_NAME);
        preparedStatement1.setDouble(2, SPO2_FREQUENCY);
        preparedStatement1.executeUpdate();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO spo2 VALUES (?, ?), (?, ?), (?, ?)");
        preparedStatement.setLong(1, SPO2_TIMESTAMP_1);
        preparedStatement.setDouble(2, SPO2_VALUE_1);
        preparedStatement.setLong(3, SPO2_TIMESTAMP_2);
        preparedStatement.setDouble(4, SPO2_VALUE_2);
        preparedStatement.setLong(5, SPO2_TIMESTAMP_3);
        preparedStatement.setDouble(6, SPO2_VALUE_3);
        preparedStatement.executeUpdate();

        SignalData actualSignalData = signalDataRepository.getAllSignalData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);

        List<Double> dataPoints = new ArrayList<>();
        dataPoints.add(SPO2_VALUE_1);
        dataPoints.add(SPO2_VALUE_2);
        SignalData expectedSignalData = new SignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STRING, dataPoints);

        assertEquals(expectedSignalData, actualSignalData);
    }

    @Test
    void getAllSignalDataRow_givenSPO2DataQuery_returnTwoItems() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO spo2 VALUES (?, ?), (?, ?), (?, ?)");
        preparedStatement.setLong(1, SPO2_TIMESTAMP_1);
        preparedStatement.setDouble(2, SPO2_VALUE_1);
        preparedStatement.setLong(3, SPO2_TIMESTAMP_2);
        preparedStatement.setDouble(4, SPO2_VALUE_2);
        preparedStatement.setLong(5, SPO2_TIMESTAMP_3);
        preparedStatement.setDouble(6, SPO2_VALUE_3);
        preparedStatement.executeUpdate();

        List<SignalDataRow> result = signalDataRepository.getAllSignalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);

        assertEquals(2, result.size());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestampMilli());
        assertEquals(SPO2_VALUE_1, result.get(0).getValue());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestampMilli());
        assertEquals(SPO2_VALUE_2, result.get(1).getValue());
    }

    @Test
    void saveSignalData_givenSPO2SignalData_storeTwoItems() throws SQLException {
        List<Double> dataPoints = new ArrayList<>();
        dataPoints.add(SPO2_VALUE_1);
        dataPoints.add(SPO2_VALUE_2);
        SignalData signalData = new SignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STRING, dataPoints);

        signalDataRepository.saveSignalData(SPO2_NAME, signalData);

        ResultSet rs = connection.prepareStatement("SELECT timestampMilli, value FROM spo2 ").executeQuery();
        assertTrue(rs.next());
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(1));
        assertEquals(SPO2_VALUE_1, rs.getDouble(2));
        assertTrue(rs.next());
        assertEquals(SPO2_TIMESTAMP_2, rs.getLong(1));
        assertEquals(SPO2_VALUE_2, rs.getDouble(2));
        assertFalse(rs.next());
    }

    @Test
    void getSignalScoreData_givenSPO2ScoreQuery_returnTwoItems() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO spo2_score VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)"
        );
        preparedStatement.setLong(1, SPO2_TIMESTAMP_1);
        preparedStatement.setLong(2, SPO2_TIMESTAMP_1);
        preparedStatement.setDouble(3, SPO2_VALUE_1);
        preparedStatement.setLong(4, SPO2_TIMESTAMP_2);
        preparedStatement.setLong(5, SPO2_TIMESTAMP_2);
        preparedStatement.setDouble(6, SPO2_VALUE_2);
        preparedStatement.setLong(7, SPO2_TIMESTAMP_3);
        preparedStatement.setLong(8, SPO2_TIMESTAMP_3);
        preparedStatement.setDouble(9, SPO2_VALUE_3);
        preparedStatement.executeUpdate();

        List<SignalScoreRow> result = signalDataRepository.getSignalScoreData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);

        assertEquals(2, result.size());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestampTo());
        assertEquals(SPO2_VALUE_1, result.get(0).getValue());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestampTo());
        assertEquals(SPO2_VALUE_2, result.get(1).getValue());
    }

    @Test
    void saveSignalScore_givenSPO2ScoreData_storeOneItem() throws SQLException {
        SignalScoreRow signalScore1 = new SignalScoreRow(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_1, SPO2_VALUE_1);

        signalDataRepository.saveSignalScore(SPO2_NAME, signalScore1);

        ResultSet rs = connection.prepareStatement(
                "SELECT timestampFrom, timestampTo, value FROM spo2_score "
        ).executeQuery();
        assertTrue(rs.next());
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(1));
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(2));
        assertEquals(SPO2_VALUE_1, rs.getDouble(3));
        assertFalse(rs.next());
    }
}