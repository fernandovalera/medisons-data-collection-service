package com.medisons.dbm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
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

    static private SignalDataRepository signalDataRepository;
    static private Connection connection;

    @Container
    static private JdbcDatabaseContainer mysql = new MySQLContainer()
            .withUsername("root")
            .withPassword("")
            .withDatabaseName("signals")
            .withInitScript("db_setup.sql");

    @BeforeAll
    static void setUpConnection () throws SQLException {
        connection = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        signalDataRepository = new SignalDataRepository(connection);
    }

    @BeforeEach
    void setUp() throws SQLException {
        clearTables();
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
        rs.next();
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(1));
        assertEquals(SPO2_VALUE_1, rs.getDouble(2));
        rs.next();
        assertEquals(SPO2_TIMESTAMP_2, rs.getLong(1));
        assertEquals(SPO2_VALUE_2, rs.getDouble(2));
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
        rs.next();
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(1));
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(2));
        assertEquals(SPO2_VALUE_1, rs.getDouble(3));
    }

    private void clearTables() throws SQLException {
        PreparedStatement truncateTableSpo2 = connection.prepareStatement("TRUNCATE TABLE spo2");
        truncateTableSpo2.executeUpdate();

        PreparedStatement truncateTableSignalInfo = connection.prepareStatement("TRUNCATE TABLE signal_info");
        truncateTableSignalInfo.executeUpdate();
    }
}