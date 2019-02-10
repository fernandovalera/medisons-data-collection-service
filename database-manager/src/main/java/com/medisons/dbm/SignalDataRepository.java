package com.medisons.dbm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SignalDataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SignalDataRepository.class.getName());

    private static final String STORE_SIGNAL_DATA_QUERY = "REPLACE INTO %s VALUES %s";
    private static final String STORE_SIGNAL_INFO_ENTRY_QUERY = "REPLACE INTO signal_info VALUES (?, ?)";
    private static final String STORE_SIGNAL_SCORE_QUERY = "REPLACE INTO %s_score VALUE (?, ?, ?)";
    private static final String STORE_OVERALL_SCORE_QUERY = "REPLACE INTO aggregated_score VALUES (?, ?, ?, ?, ?, ?)";
    private static final String GET_SIGNAL_DATA_QUERY = "SELECT timestamp, value FROM %s WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
    private static final String GET_SIGNAL_FREQUENCY_QUERY = "SELECT frequency FROM signal_info WHERE name = ?";
    private static final String GET_SIGNAL_SCORE_QUERY = "SELECT timestampFrom, timestampTo, value FROM %s_score WHERE timestampFrom >= ? AND timestampTo <= ?"
            + " ORDER BY timestampFrom";
    private static final String GET_SIGNAL_SCORE_TABLE_NAMES_QUERY = "SELECT table_name FROM information_schema.tables WHERE table_schema='signals' AND"
            + " TABLE_NAME like '%_score'";
    private static final String GET_LAST_SCORE_IN_RANGE_QUERY = "SELECT * FROM %s where timestampTo BETWEEN ? AND ? ORDER BY timestampTo DESC LIMIT 1";

    private static final String DATA_TIMESTAMP_COLUMN = "timestamp";
    private static final String VALUE_COLUMN = "value";
    private static final String SCORE_FROM_COLUMN = "timestampFrom";
    private static final String SCORE_TO_COLUMN = "timestampTo";
    private static final String TABLE_NAME_COLUMN = "table_name";
    private static final String SCORE_TABLE_NAME_SUFFIX = "_score";

    private final Connection signalDataConnection;

    public SignalDataRepository(Connection signalDataConnection) {
        this.signalDataConnection = signalDataConnection;
    }

    private boolean tableExists(String signalName) {
        try {
            DatabaseMetaData dbm = signalDataConnection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, signalName, null);
            if (tables.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            LOG.debug(e.getMessage());
        }
        return false;
    }

    public SignalData getAllSignalData(String signalName, long from, long to) throws SignalDataDBException {
        SignalData signalData;

        try {
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(GET_SIGNAL_FREQUENCY_QUERY);
            preparedStatement.setString(1, signalName);
            ResultSet signalFrequencyResultSet = preparedStatement.executeQuery();

            if (signalFrequencyResultSet.next()) {
                double frequency = signalFrequencyResultSet.getDouble(1);

                String dataPointsQuery = String.format(GET_SIGNAL_DATA_QUERY, signalName);
                PreparedStatement dataPointsPreparedStatement = signalDataConnection.prepareStatement(dataPointsQuery);
                dataPointsPreparedStatement.setLong(1, from);
                dataPointsPreparedStatement.setLong(2, to);
                ResultSet dataPointsResultSet = dataPointsPreparedStatement.executeQuery();

                signalData = newSignalData(signalName, frequency, dataPointsResultSet);
            } else {
                LOG.error("Input signal name '{}' is not supported.", signalName);
                throw new SignalDataDBException();
            }
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalData;
    }

    public List<SignalDataRow> getAllSignalDataRow(String signalName, long from, long to) throws SignalDataDBException {
        List<SignalDataRow> allSignalDataRow = new ArrayList<>();
        String query = String.format(GET_SIGNAL_DATA_QUERY, signalName);

        try {
            if (tableExists(signalName)) {
                PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
                preparedStatement.setLong(1, from);
                preparedStatement.setLong(2, to);
                ResultSet rs = preparedStatement.executeQuery();

                while (rs.next()) {
                    allSignalDataRow.add(newSignalDataRow(rs));
                }
            } else {
                LOG.error("Input signal name '{}' is not supported.", signalName);
                throw new SignalDataDBException();
            }
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }

        return allSignalDataRow;
    }

    public void saveSignalData(String signalName, SignalData signalData) throws SignalDataDBException {
        StringBuilder dataPointsString = new StringBuilder();

        if (!tableExists(signalName)) {
            LOG.error("Could not find table for signal '{}'.", signalName);
            throw new SignalDataDBException();
        }

        // Create and execute signal info update query
        double frequency = signalData.getFrequency();
        try {
            PreparedStatement statement = this.signalDataConnection.prepareStatement(STORE_SIGNAL_INFO_ENTRY_QUERY);
            statement.setString(1, signalName);
            statement.setDouble(2, frequency);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }

        // Convert data points list to a comma separated string
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
            Date date = simpleDateFormat.parse(signalData.getTimestamp());
            calendar.setTime(date);

            // calendar will convert local date to UTC and then get epoch millis
            long timeInMS = calendar.getTimeInMillis();
            for (Double dataPoint : signalData.getDataPoints()) {
                dataPointsString.append(String.format("(%s,%s),", timeInMS, dataPoint));
                timeInMS = timeInMS + (long) (1000 / frequency);
            }

            // Remove last comma
            dataPointsString.deleteCharAt(dataPointsString.length()-1);
        }
        catch (ParseException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }

        // Create and execute signal data update query
        String query = String.format(STORE_SIGNAL_DATA_QUERY, signalName, dataPointsString);
        try {
            Statement statement = signalDataConnection.createStatement();
            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    public List<SignalScoreRow> getSignalScoreData (String signalName, long from, long to) throws SignalDataDBException {
        List<SignalScoreRow> signalScoreData = new ArrayList<>();
        String query = String.format(GET_SIGNAL_SCORE_QUERY, signalName);

        try {
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
            preparedStatement.setLong(1, from);
            preparedStatement.setLong(2, to);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                signalScoreData.add(newSignalScoreRow(rs));
            }
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalScoreData;
    }

    public List<SignalScoreRowListItem> getLastSignalScoreRowsInRange(long from, long to) throws SignalDataDBException {
        List<SignalScoreRowListItem> signalScoreRows = new ArrayList<>();

        // get names of score tables
        List<String> scoreTableNames = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(
                    GET_SIGNAL_SCORE_TABLE_NAMES_QUERY);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                scoreTableNames.add(rs.getString(TABLE_NAME_COLUMN));
            }
        }
        catch (SQLException e) {
            LOG.error("Error executing get signal score table names query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        // get last score in provided range for each score table
        try {
            for (String scoreTableName : scoreTableNames) {
                String getLastScoreInRangeQuery = String.format(GET_LAST_SCORE_IN_RANGE_QUERY, scoreTableName);
                PreparedStatement preparedStatement = signalDataConnection.prepareStatement(getLastScoreInRangeQuery);
                preparedStatement.setLong(1, from);
                preparedStatement.setLong(2, to);
                ResultSet rs = preparedStatement.executeQuery();

                // max one row expected
                if (rs.next()) {
                    String signalName = scoreTableName.substring(0, scoreTableName.indexOf(SCORE_TABLE_NAME_SUFFIX));
                    signalScoreRows.add(newSignalScoreRowListItem(signalName, rs));
                }
            }
        }
        catch (SQLException e) {
            LOG.error("Error executing get last score in range query for table: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalScoreRows;
    }

    public void saveSignalScore(String signalName, SignalScoreRow signalScoreRow) throws SignalDataDBException {
        String query = String.format(STORE_SIGNAL_SCORE_QUERY, signalName);

        try {
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
            preparedStatement.setLong(1, signalScoreRow.getTimestampFrom());
            preparedStatement.setLong(2, signalScoreRow.getTimestampTo());
            preparedStatement.setDouble(3, signalScoreRow.getValue());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LOG.error(e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    public void saveOverallScore(OverallScoreRow overallScoreRow) throws SignalDataDBException {
        try {
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(STORE_OVERALL_SCORE_QUERY);
            preparedStatement.setLong(1, overallScoreRow.getTimestamp());
            preparedStatement.setDouble(2, overallScoreRow.getValue());
            if (overallScoreRow.getSpo2() == null) {
                preparedStatement.setNull(3, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(3, overallScoreRow.getSpo2());
            }
            if (overallScoreRow.getEcg() == null) {
                preparedStatement.setNull(4, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(4, overallScoreRow.getEcg());
            }
            if (overallScoreRow.getResp() == null) {
                preparedStatement.setNull(5, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(5, overallScoreRow.getResp());
            }
            if (overallScoreRow.getTemp() == null) {
                preparedStatement.setNull(6, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(6, overallScoreRow.getTemp());
            }

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LOG.error("Error executing store overall score query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    private SignalData newSignalData(String signalName, double frequency, ResultSet rs) throws SQLException {
        long timeInMS = -1;
        List<Double> dataPoints = new ArrayList<>();

        while (rs.next()) {
            if (timeInMS == -1) {
                timeInMS = rs.getLong(1);
            }
            dataPoints.add(rs.getDouble(2));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        // date string will be returned as a UTC time
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = simpleDateFormat.format(new Date(timeInMS));

        return new SignalData(signalName, frequency, timestamp, dataPoints);
    }

    private SignalDataRow newSignalDataRow(ResultSet rs) throws SQLException {
        return new SignalDataRow(rs.getLong(DATA_TIMESTAMP_COLUMN), rs.getDouble(VALUE_COLUMN));
    }

    private SignalScoreRow newSignalScoreRow(ResultSet rs) throws SQLException {
        return new SignalScoreRow(rs.getLong(SCORE_FROM_COLUMN), rs.getLong(SCORE_TO_COLUMN),
                rs.getDouble(VALUE_COLUMN));
    }

    private SignalScoreRowListItem newSignalScoreRowListItem(String name, ResultSet rs) throws SQLException {
        SignalScoreRow newSignalScoreRow = newSignalScoreRow(rs);
        return new SignalScoreRowListItem(name, newSignalScoreRow);
    }

    public class SignalDataDBException extends Exception {

        public SignalDataDBException() {
            super();
        }

        public SignalDataDBException(Exception e) {
            super(e);
        }
    }
}
