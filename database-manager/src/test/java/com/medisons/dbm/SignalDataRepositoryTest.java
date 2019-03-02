package com.medisons.dbm;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SignalDataRepositoryTest {

    private static final String SPO2_NAME = "spo2";
    private static final String ECG_NAME = "ecg";
    private static final String BP_NAME = "bp";
    private static final String BP_DIA_NAME = "bp_dia";
    private static final String BP_SYS_NAME = "bp_sys";

    private static final double SPO2_FREQUENCY = 1.0d;

    private static final String SPO2_TIMESTAMP_STRING = "2019.01.01 00:00:00.000";
    private static final long SPO2_TIMESTAMP_1 = 1546300800000L;
    private static final long SPO2_TIMESTAMP_2 = 1546300801000L;
    private static final long SPO2_TIMESTAMP_3 = 1546300802000L;
    private static final long ECG_TIMESTAMP_1 = 1546300801000L;
    private static final long ECG_TIMESTAMP_2 = 1546300802000L;
    private static final long ECG_TIMESTAMP_3 = 1546300803000L;

    private static final double SPO2_VALUE_1 = 1.5D;
    private static final double SPO2_VALUE_2 = 2.7D;
    private static final double SPO2_VALUE_3 = 3.4D;
    private static final double ECG_VALUE_1 = 9.1D;
    private static final double ECG_VALUE_2 = 6.3D;
    private static final double ECG_VALUE_3 = 1.9D;

    private static final long AGGREGATE_TIMESTAMP_1 = 1546300802000L;
    private static final double AGGREGATE_VALUE_1 = 7.5;
    private static final Double AGGREGATE_SPO2_1 = 1.5;
    private static final Double AGGREGATE_ECG_1 = 3.0;
    private static final Double AGGREGATE_BP_1 = 0.0;
    private static final Double AGGREGATE_RESP_1 = 2.0;
    private static final Double AGGREGATE_TEMP_1 = 1.0;
    private static final long AGGREGATE_TIMESTAMP_2 = 1546300803000L;
    private static final double AGGREGATE_VALUE_2 = 5.0;
    private static final Double AGGREGATE_SPO2_2 = 1.0;
    private static final Double AGGREGATE_ECG_2 = 1.0;
    private static final Double AGGREGATE_BP_2 = 1.0;
    private static final Double AGGREGATE_RESP_2 = 1.0;
    private static final Double AGGREGATE_TEMP_2 = 1.0;
    private static final long AGGREGATE_TIMESTAMP_3 = 1546300804000L;
    private static final double AGGREGATE_VALUE_3 = 3.0;
    private static final Double AGGREGATE_SPO2_3 = 1.0;
    private static final Double AGGREGATE_ECG_3 = 2.0;
    private static final Double AGGREGATE_BP_3 = null;
    private static final Double AGGREGATE_RESP_3 = null;
    private static final Double AGGREGATE_TEMP_3 = null;


    private static final String INVALID_SIGNAL_NAME = "yo";

    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB = "test_signals";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Flyway flyway;

    @Spy
    private Connection connection;

    private static ConnectionManager connectionManager;

    private SignalDataRepository signalDataRepository;

    @BeforeAll
    static void setUpConnection () throws SQLException {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerTimezone("UTC");
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);
        dataSource.setURL(URL);
        Connection connection = dataSource.getConnection();

        try {
            connection.prepareStatement(String.format("CREATE DATABASE %s", DB)).executeUpdate();
        } catch (SQLException e) {
            // Database already exists.
        }
        connection.prepareStatement(String.format("USE %s", DB)).executeUpdate();

        dataSource.setDatabaseName(DB);
        flyway = Flyway.configure().schemas(DB).dataSource(dataSource).load();

        connectionManager = new HikariConnectionManager(URL + DB + "?serverTimezone=UTC");
    }

    @BeforeEach
    void setUp() throws SQLException {
        flyway.clean();
        flyway.migrate();

        signalDataRepository = new SignalDataRepository(connectionManager);
        connection = spy(connectionManager.getConnection());
    }

    @AfterEach
    void tearDown() throws SQLException {
        signalDataRepository = null;
        connection.close();
    }

    @Test
    void getSignalTableNamesFromBaseName_givenBP_returnTwoTables() {
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add(BP_DIA_NAME);
        expectedResult.add(BP_SYS_NAME);
        List<String> actualResult = null;
        try {
            actualResult = signalDataRepository.getSignalTableNamesFromBaseName(BP_NAME);
        } catch (SignalDataRepository.SignalDataDBException e) {
            fail();
        }
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void getSignalTableNamesFromBaseName_givenSPO2_returnOneTable() {
        List<String> expectedResult = new ArrayList<>();
        expectedResult.add(SPO2_NAME);
        List<String> actualResult = null;
        try {
            actualResult = signalDataRepository.getSignalTableNamesFromBaseName(SPO2_NAME);
        } catch (SignalDataRepository.SignalDataDBException e) {
            fail();
        }
        assertEquals(expectedResult, actualResult);
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

        SignalDataList actualSignalDataList = null;
        try {
            actualSignalDataList = signalDataRepository.getAllSignalData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        } catch (Exception e) {
            fail();
        }

        List<Long> timestamps = new ArrayList<>();
        timestamps.add(SPO2_TIMESTAMP_1);
        timestamps.add(SPO2_TIMESTAMP_2);

        List<Double> values = new ArrayList<>();
        values.add(SPO2_VALUE_1);
        values.add(SPO2_VALUE_2);
        SignalDataList expectedSignalDataList = new SignalDataList(SPO2_NAME, timestamps, values);

        assertEquals(expectedSignalDataList, actualSignalDataList);
    }

    @Test
    void getAllSignalData_givenInvalidSignalName_throwsException() {
        try {
            signalDataRepository.getAllSignalData(INVALID_SIGNAL_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void getAllSignalData_givenDBError_throwsException() {
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.getAllSignalData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
            fail();
        } catch (Exception ignored) {

        }
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

        List<SignalDataRow> result = null;
        try {
            result = signalDataRepository.getAllSignalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        } catch (Exception e) {
            fail();
        }

        assertEquals(2, result.size());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestamp());
        assertEquals(SPO2_VALUE_1, result.get(0).getValue());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestamp());
        assertEquals(SPO2_VALUE_2, result.get(1).getValue());
    }

    @Test
    void getAllSignalDataRow_givenInvalidSignalName_throwsException() {
        try {
            signalDataRepository.getAllSignalDataRow(INVALID_SIGNAL_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void getAllSignalDataRow_givenDBError_throwsException() {
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.getAllSignalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void saveSignalData_givenSPO2SignalData_storeTwoItems() throws SQLException, ParseException {
        List<Double> dataPoints = new ArrayList<>();
        dataPoints.add(SPO2_VALUE_1);
        dataPoints.add(SPO2_VALUE_2);
        // SPO2_Timestamp_string is in local timezone
        SignalData signalData = new SignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STRING, dataPoints);

        try {
            signalDataRepository.saveSignalData(SPO2_NAME, signalData);
        } catch (Exception e) {
            fail();
        }

        // inserted time stamps should be in UTC, calendar converts local date to UTC
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        Date date = simpleDateFormat.parse(SPO2_TIMESTAMP_STRING);
        calendar.setTime(date);
        long expectedTimeStamp1InMS = calendar.getTimeInMillis();
        long expectedTimeStamp2InMS = expectedTimeStamp1InMS + 1000L;

        ResultSet rs = connection.prepareStatement("SELECT timestamp, value FROM spo2").executeQuery();
        assertTrue(rs.next());
        assertEquals(expectedTimeStamp1InMS, rs.getLong(1));
        assertEquals(SPO2_VALUE_1, rs.getDouble(2));
        assertTrue(rs.next());
        assertEquals(expectedTimeStamp2InMS, rs.getLong(1));
        assertEquals(SPO2_VALUE_2, rs.getDouble(2));
        assertFalse(rs.next());
    }

    @Test
    void saveSignalData_givenInvalidSignalName_throwsException() {
        try {
            signalDataRepository.saveSignalData(INVALID_SIGNAL_NAME, null);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void saveSignalData_givenDBError_throwsException() {
        List<Double> dataPoints = new ArrayList<>();
        dataPoints.add(SPO2_VALUE_1);
        dataPoints.add(SPO2_VALUE_2);
        SignalData signalData = new SignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STRING, dataPoints);

        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.saveSignalData(SPO2_NAME, signalData);
            fail();
        } catch (Exception ignored) {

        }
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

        List<SignalScoreRow> result = null;
        try {
            result = signalDataRepository.getSignalScoreData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        } catch (Exception e) {
            fail();
        }

        assertEquals(2, result.size());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_1, result.get(0).getTimestampTo());
        assertEquals(SPO2_VALUE_1, result.get(0).getValue());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_2, result.get(1).getTimestampTo());
        assertEquals(SPO2_VALUE_2, result.get(1).getValue());
    }

    @Test
    void getSignalScoreData_givenDBError_throwsException() {
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.getSignalScoreData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void saveSignalScore_givenSPO2ScoreData_storeOneItem() throws SQLException {
        SignalScoreRow signalScore1 = new SignalScoreRow(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_1, SPO2_VALUE_1);

        try {
            signalDataRepository.saveSignalScore(SPO2_NAME, signalScore1);
        } catch (Exception e) {
            fail();
        }

        ResultSet rs = connection.prepareStatement(
                "SELECT timestampFrom, timestampTo, value FROM spo2_score "
        ).executeQuery();
        assertTrue(rs.next());
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(1));
        assertEquals(SPO2_TIMESTAMP_1, rs.getLong(2));
        assertEquals(SPO2_VALUE_1, rs.getDouble(3));
        assertFalse(rs.next());
    }

    @Test
    void saveSignalScore_givenDBError_throwsException() {
        SignalScoreRow signalScore1 = new SignalScoreRow(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_1, SPO2_VALUE_1);

        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.saveSignalScore(SPO2_NAME, signalScore1);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void getLastSignalScoreRowsInRange_lastScoreReturned() throws SQLException {
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

        List<SignalScoreRowListItem> result = null;
        try {
            result = signalDataRepository.getLastSignalScoreRowsInRange(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_3);
        }
        catch (Exception e) {
            fail();
        }

        assertEquals(1, result.size());
        SignalScoreRowListItem scoreRow = result.get(0);
        assertEquals(SPO2_NAME, scoreRow.getName());
        assertEquals(SPO2_TIMESTAMP_3, scoreRow.getScore().getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_3, scoreRow.getScore().getTimestampTo());
        assertEquals(SPO2_VALUE_3, scoreRow.getScore().getValue());
    }

    @Test
    void getLastSignalScoreRowsInRange_lastScoreReturned_twoVitals() throws SQLException {
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

        PreparedStatement preparedStatement2 = connection.prepareStatement(
                "INSERT INTO ecg_score VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)"
        );
        preparedStatement2.setLong(1, ECG_TIMESTAMP_1);
        preparedStatement2.setLong(2, ECG_TIMESTAMP_1);
        preparedStatement2.setDouble(3, ECG_VALUE_1);
        preparedStatement2.setLong(4, ECG_TIMESTAMP_2);
        preparedStatement2.setLong(5, ECG_TIMESTAMP_2);
        preparedStatement2.setDouble(6, ECG_VALUE_2);
        preparedStatement2.setLong(7, ECG_TIMESTAMP_3);
        preparedStatement2.setLong(8, ECG_TIMESTAMP_3);
        preparedStatement2.setDouble(9, ECG_VALUE_3);
        preparedStatement2.executeUpdate();

        List<SignalScoreRowListItem> result = null;
        try {
            result = signalDataRepository.getLastSignalScoreRowsInRange(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_3);
        }
        catch (Exception e) {
            fail();
        }

        assertEquals(2, result.size());
        SignalScoreRowListItem spo2ScoreRow = getSignalScoreRowByName(SPO2_NAME, result);
        SignalScoreRowListItem ecgScoreRow = getSignalScoreRowByName(ECG_NAME, result);
        if (spo2ScoreRow == null || ecgScoreRow == null) {
            fail("Expected to get an spo2 and ecg score.");
        }

        assertEquals(SPO2_NAME, spo2ScoreRow.getName());
        assertEquals(SPO2_TIMESTAMP_3, spo2ScoreRow.getScore().getTimestampFrom());
        assertEquals(SPO2_TIMESTAMP_3, spo2ScoreRow.getScore().getTimestampTo());
        assertEquals(SPO2_VALUE_3, spo2ScoreRow.getScore().getValue());

        assertEquals(ECG_NAME, ecgScoreRow.getName());
        assertEquals(ECG_TIMESTAMP_2, ecgScoreRow.getScore().getTimestampFrom());
        assertEquals(ECG_TIMESTAMP_2, ecgScoreRow.getScore().getTimestampTo());
        assertEquals(ECG_VALUE_2, ecgScoreRow.getScore().getValue());
    }

    @Test
    void getLastSignalScoreRowsInRange_throwsException() {
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.getLastSignalScoreRowsInRange(SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_3);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void saveAggregatedScore_noNullValues() throws SQLException {
        AggregatedScoreRow aggregatedScoreRow = new AggregatedScoreRow(AGGREGATE_TIMESTAMP_1, AGGREGATE_VALUE_1, AGGREGATE_SPO2_1,
                AGGREGATE_ECG_1, AGGREGATE_BP_1, AGGREGATE_RESP_1, AGGREGATE_TEMP_1);
        try {
            signalDataRepository.saveAggregatedScore(aggregatedScoreRow);
        } catch (Exception e) {
            fail();
        }

        ResultSet rs = connection.prepareStatement(
                "SELECT timestamp, value, spo2_score, ecg_score, bp_score, resp_rate_score, temperature_score FROM aggregated_score"
        ).executeQuery();

        assertTrue(rs.next());
        assertEquals(AGGREGATE_TIMESTAMP_1, rs.getLong(1));
        assertEquals(AGGREGATE_VALUE_1, rs.getDouble(2));
        assertEquals((double) AGGREGATE_SPO2_1, rs.getDouble(3));
        assertEquals((double) AGGREGATE_ECG_1, rs.getDouble(4));
        assertEquals((double) AGGREGATE_BP_1, rs.getDouble(5));
        assertEquals((double) AGGREGATE_RESP_1, rs.getDouble(6));
        assertEquals((double) AGGREGATE_TEMP_1, rs.getDouble(7));
        assertFalse(rs.next());
    }

    @Test
    void saveAggregatedScore_nullValues() throws SQLException {
        AggregatedScoreRow aggregatedScoreRow = new AggregatedScoreRow(AGGREGATE_TIMESTAMP_3, AGGREGATE_VALUE_3, AGGREGATE_SPO2_3,
                AGGREGATE_ECG_3, AGGREGATE_BP_3, AGGREGATE_RESP_3, AGGREGATE_TEMP_3);
        try {
            signalDataRepository.saveAggregatedScore(aggregatedScoreRow);
        } catch (Exception e) {
            fail();
        }

        ResultSet rs = connection.prepareStatement(
                "SELECT timestamp, value, spo2_score, ecg_score, bp_score, resp_rate_score, temperature_score FROM aggregated_score"
        ).executeQuery();

        assertTrue(rs.next());
        assertEquals(AGGREGATE_TIMESTAMP_3, rs.getLong(1));
        assertEquals(AGGREGATE_VALUE_3, rs.getDouble(2));
        assertEquals((double) AGGREGATE_SPO2_3, rs.getDouble(3));
        assertEquals((double) AGGREGATE_ECG_3, rs.getDouble(4));
        rs.getDouble(5);
        assertTrue(rs.wasNull());
        rs.getDouble(6);
        assertTrue(rs.wasNull());
        rs.getDouble(7);
        assertTrue(rs.wasNull());
    }

    @Test
    void saveAggregatedScore_throwsException() {
        AggregatedScoreRow aggregatedScoreRow = new AggregatedScoreRow(AGGREGATE_TIMESTAMP_1, AGGREGATE_VALUE_1, AGGREGATE_SPO2_1,
                AGGREGATE_ECG_1, AGGREGATE_BP_1, AGGREGATE_RESP_1, AGGREGATE_TEMP_1);
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.saveAggregatedScore(aggregatedScoreRow);
            fail();
        } catch (Exception ignored) {

        }
    }

    @Test
    void getAggregatedScoreRowList_twoRows_noNullComponents() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO aggregated_score VALUES (?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?)"
        );
        preparedStatement.setLong(1, AGGREGATE_TIMESTAMP_1);
        preparedStatement.setDouble(2, AGGREGATE_VALUE_1);
        preparedStatement.setDouble(3, AGGREGATE_SPO2_1);
        preparedStatement.setDouble(4, AGGREGATE_ECG_1);
        preparedStatement.setDouble(5, AGGREGATE_BP_1);
        preparedStatement.setDouble(6, AGGREGATE_RESP_1);
        preparedStatement.setDouble(7, AGGREGATE_TEMP_1);
        preparedStatement.setLong(8, AGGREGATE_TIMESTAMP_2);
        preparedStatement.setDouble(9, AGGREGATE_VALUE_2);
        preparedStatement.setDouble(10, AGGREGATE_SPO2_2);
        preparedStatement.setDouble(11, AGGREGATE_ECG_2);
        preparedStatement.setDouble(12, AGGREGATE_BP_2);
        preparedStatement.setDouble(13, AGGREGATE_RESP_2);
        preparedStatement.setDouble(14, AGGREGATE_TEMP_2);
        preparedStatement.executeUpdate();

        AggregatedScoreRowList result = null;
        try {
            result = signalDataRepository.getAggregatedScoreRowList(AGGREGATE_TIMESTAMP_1, AGGREGATE_TIMESTAMP_2);
        }
        catch (Exception e) {
            fail();
        }

        AggregatedScoreRowList expectedList = getExpectedAggregatedScoreRowList();
        assertEquals(expectedList.getTimestamp(), result.getTimestamp());
        assertEquals(expectedList.getValue(), result.getValue());
        assertEquals(expectedList.getSpo2(), result.getSpo2());
        assertEquals(expectedList.getEcg(), result.getEcg());
        assertEquals(expectedList.getBp(), result.getBp());
        assertEquals(expectedList.getResp(), result.getResp());
        assertEquals(expectedList.getTemp(), result.getTemp());
    }

    @Test
    void getAggregatedScoreRowList_twoRows_nullComponents() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO aggregated_score VALUES (?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?)"
        );
        preparedStatement.setLong(1, AGGREGATE_TIMESTAMP_1);
        preparedStatement.setDouble(2, AGGREGATE_VALUE_1);
        preparedStatement.setDouble(3, AGGREGATE_SPO2_1);
        preparedStatement.setDouble(4, AGGREGATE_ECG_1);
        preparedStatement.setDouble(5, AGGREGATE_BP_1);
        preparedStatement.setDouble(6, AGGREGATE_RESP_1);
        preparedStatement.setDouble(7, AGGREGATE_TEMP_1);
        preparedStatement.setLong(8, AGGREGATE_TIMESTAMP_3);
        preparedStatement.setDouble(9, AGGREGATE_VALUE_3);
        preparedStatement.setDouble(10, AGGREGATE_SPO2_3);
        preparedStatement.setDouble(11, AGGREGATE_ECG_3);
        preparedStatement.setNull(12, Types.DOUBLE);
        preparedStatement.setNull(13, Types.DOUBLE);
        preparedStatement.setNull(14, Types.DOUBLE);
        preparedStatement.executeUpdate();

        AggregatedScoreRowList result = null;
        try {
            result = signalDataRepository.getAggregatedScoreRowList(AGGREGATE_TIMESTAMP_1, AGGREGATE_TIMESTAMP_3);
        }
        catch (Exception e) {
            fail();
        }

        AggregatedScoreRowList expectedList = getExpectedAggregatedScoreRowListNullValues();
        assertEquals(expectedList.getTimestamp(), result.getTimestamp());
        assertEquals(expectedList.getValue(), result.getValue());
        assertEquals(expectedList.getSpo2(), result.getSpo2());
        assertEquals(expectedList.getEcg(), result.getEcg());
        assertEquals(expectedList.getBp(), result.getBp());
        assertEquals(expectedList.getResp(), result.getResp());
        assertEquals(expectedList.getTemp(), result.getTemp());
    }

    @Test
    void getAggregatedScoreRowList_throwsException() {
        try {
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            signalDataRepository.getAggregatedScoreRowList(100L, 200L);
            fail();
        } catch (Exception ignored) {

        }
    }

    private SignalScoreRowListItem getSignalScoreRowByName(String signalName, List<SignalScoreRowListItem> scores) {
        for (SignalScoreRowListItem score : scores) {
            if (score.getName().equals(signalName)) {
                return score;
            }
        }
        return null;
    }

    private AggregatedScoreRowList getExpectedAggregatedScoreRowList() {
        List<Long> timestamp = new ArrayList<>();
        timestamp.add(AGGREGATE_TIMESTAMP_1);
        timestamp.add(AGGREGATE_TIMESTAMP_2);
        List<Double> value = new ArrayList<>();
        value.add(AGGREGATE_VALUE_1);
        value.add(AGGREGATE_VALUE_2);
        List<Double> spo2 = new ArrayList<>();
        spo2.add(AGGREGATE_SPO2_1);
        spo2.add(AGGREGATE_SPO2_2);
        List<Double> ecg = new ArrayList<>();
        ecg.add(AGGREGATE_ECG_1);
        ecg.add(AGGREGATE_ECG_2);
        List<Double> bp = new ArrayList<>();
        bp.add(AGGREGATE_BP_1);
        bp.add(AGGREGATE_BP_2);
        List<Double> resp = new ArrayList<>();
        resp.add(AGGREGATE_RESP_1);
        resp.add(AGGREGATE_RESP_2);
        List<Double> temp = new ArrayList<>();
        temp.add(AGGREGATE_TEMP_1);
        temp.add(AGGREGATE_TEMP_2);
        return new AggregatedScoreRowList(timestamp, value, spo2, ecg, bp, resp, temp);
    }

    private AggregatedScoreRowList getExpectedAggregatedScoreRowListNullValues() {
        List<Long> timestamp = new ArrayList<>();
        timestamp.add(AGGREGATE_TIMESTAMP_1);
        timestamp.add(AGGREGATE_TIMESTAMP_3);
        List<Double> value = new ArrayList<>();
        value.add(AGGREGATE_VALUE_1);
        value.add(AGGREGATE_VALUE_3);
        List<Double> spo2 = new ArrayList<>();
        spo2.add(AGGREGATE_SPO2_1);
        spo2.add(AGGREGATE_SPO2_3);
        List<Double> ecg = new ArrayList<>();
        ecg.add(AGGREGATE_ECG_1);
        ecg.add(AGGREGATE_ECG_3);
        List<Double> bp = new ArrayList<>();
        bp.add(AGGREGATE_BP_1);
        bp.add(AGGREGATE_BP_3);
        List<Double> resp = new ArrayList<>();
        resp.add(AGGREGATE_RESP_1);
        resp.add(AGGREGATE_RESP_3);
        List<Double> temp = new ArrayList<>();
        temp.add(AGGREGATE_TEMP_1);
        temp.add(AGGREGATE_TEMP_3);
        return new AggregatedScoreRowList(timestamp, value, spo2, ecg, bp, resp, temp);
    }
}
