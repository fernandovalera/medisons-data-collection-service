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
import java.util.*;

public class SignalDataRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SignalDataRepository.class.getName());

    private static final String STORE_SIGNAL_DATA_QUERY = "REPLACE INTO %s VALUES %s";
    private static final String STORE_SIGNAL_INFO_ENTRY_QUERY = "REPLACE INTO signal_info VALUES (?, ?)";
    private static final String STORE_SIGNAL_SCORE_QUERY = "REPLACE INTO %s_score VALUE (?, ?, ?)";
    private static final String STORE_AGGREGATED_SCORE_QUERY = "REPLACE INTO aggregated_score VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String GET_SIGNAL_DATA_QUERY = "SELECT timestamp, value FROM %s WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
    private static final String GET_SIGNAL_FREQUENCY_QUERY = "SELECT frequency FROM signal_info WHERE name = ?";
    private static final String GET_SIGNAL_SCORE_QUERY = "SELECT timestampFrom, timestampTo, value FROM %s_score WHERE timestampFrom >= ? AND timestampTo <= ?"
            + " ORDER BY timestampFrom";
    private static final String GET_SIGNAL_SCORE_TABLE_NAMES_QUERY = "SELECT table_name FROM information_schema.tables WHERE table_schema='signals' AND"
            + " table_name like '%_score' AND table_name <> 'aggregated_score'";
    private static final String GET_LAST_SCORE_IN_RANGE_QUERY = "SELECT * FROM %s where timestampTo BETWEEN ? AND ? ORDER BY timestampTo DESC LIMIT 1";
    private static final String GET_AGGREGATED_SCORE_QUERY = "SELECT timestamp, value, spo2_score, ecg_score, bp_score, resp_rate_score, temperature_score FROM"
            + " aggregated_score WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
    private static final String DATA_TIMESTAMP_COLUMN = "timestamp";
    private static final String VALUE_COLUMN = "value";
    private static final String SCORE_FROM_COLUMN = "timestampFrom";
    private static final String SCORE_TO_COLUMN = "timestampTo";
    private static final String TABLE_NAME_COLUMN = "table_name";
    private static final String SCORE_TABLE_NAME_SUFFIX = "_score";
    private static final String AGGREGATED_SCORE_SPO2_COLUMN = "spo2_score";
    private static final String AGGREGATED_SCORE_ECG_COLUMN = "ecg_score";
    private static final String AGGREGATED_SCORE_BP_COLUMN = "bp_score";
    private static final String AGGREGATED_SCORE_RESP_COLUMN = "resp_rate_score";
    private static final String AGGREGATED_SCORE_TEMP_COLUMN = "temperature_score";

    private ConnectionManager connectionManager;

    public SignalDataRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    private boolean tableExists(String signalName, Connection connection) {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            try (ResultSet tables = dbm.getTables(connection.getCatalog(), null, signalName,
                    new String[] {"TABLE"})) {
                if (tables.next()) {
                    return true;
                }
            }
        }
        catch (SQLException e) {
            LOG.debug(e.getMessage());
        }
        return false;
    }

    public List<String> getSignalTableNamesFromBaseName(String baseSignalName) throws SignalDataDBException {
        List<String> signalNamesList = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection()){
            String tableNamePattern = baseSignalName + "%";
            DatabaseMetaData dbm = connection.getMetaData();
            try (ResultSet tables = dbm.getTables(connection.getCatalog(), null, tableNamePattern,
                    new String[] {"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if (!tableName.contains("score")) {
                        signalNamesList.add(tableName);
                    }
                }
            }
        }
        catch (SQLException e) {
            LOG.debug(e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalNamesList;
    }

    public SignalDataRowList getSignalDataRowList(String signalName, long from, long to) throws SignalDataDBException {
        SignalDataRowList signalDataList;

        try (Connection connection = connectionManager.getConnection())
        {
            if (tableExists(signalName, connection))
            {
                Double frequency;
                try (PreparedStatement frequencyPreparedStatement =
                             connection.prepareStatement(GET_SIGNAL_FREQUENCY_QUERY))
                {
                    frequencyPreparedStatement.setString(1, signalName);
                    try (ResultSet frequencyResultSet = frequencyPreparedStatement.executeQuery())
                    {
                        frequencyResultSet.next();
                        frequency = frequencyResultSet.getDouble("frequency");
                    }
                }
                catch (SQLException e) {
                    LOG.error("Failed to get frequency for signal name '{}'", signalName);
                    throw new SignalDataDBException(e);
                }

                String dataPointsQuery = String.format(GET_SIGNAL_DATA_QUERY, signalName);
                try (PreparedStatement dataPointsPreparedStatement = connection.prepareStatement(dataPointsQuery))
                {
                    dataPointsPreparedStatement.setLong(1, from);
                    dataPointsPreparedStatement.setLong(2, to);
                    try (ResultSet dataPointsResultSet = dataPointsPreparedStatement.executeQuery())
                    {
                        signalDataList = newSignalDataList(signalName, frequency, dataPointsResultSet);
                    }
                }
                catch (SQLException e) {
                    LOG.error("Failed to get data for signal name '{}'", signalName);
                    throw new SignalDataDBException(e);
                }
            }
            else
            {
                LOG.error("Input signal name '{}' is not supported.", signalName);
                throw new SignalDataDBException();
            }
        }
        catch (SQLException e) {
            LOG.error("Error getting connection: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalDataList;
    }

    public void saveSignalData(String signalName, SignalData signalData) throws SignalDataDBException {
        StringBuilder dataPointsString = new StringBuilder();

        try (Connection connection = connectionManager.getConnection()) {
            if (!tableExists(signalName, connection)) {
                LOG.error("Could not find table for signal '{}'.", signalName);
                throw new SignalDataDBException();
            }

            // Create and execute signal info update query
            double frequency = signalData.getFrequency();
            try (PreparedStatement statement = connection.prepareStatement(STORE_SIGNAL_INFO_ENTRY_QUERY)) {
                statement.setString(1, signalName);
                statement.setDouble(2, frequency);
                statement.executeUpdate();
            } catch (SQLException e) {
                LOG.error("Error executing store signal info entry query: " + e.getMessage());
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
                dataPointsString.deleteCharAt(dataPointsString.length() - 1);
            } catch (ParseException e) {
                LOG.error("Error parsing signal data to store: " + e.getMessage());
                throw new SignalDataDBException(e);
            }

            // Create and execute signal data update query
            String query = String.format(STORE_SIGNAL_DATA_QUERY, signalName, dataPointsString);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(query);
            } catch (SQLException e) {
                LOG.error("Error executing store signal data query: " + e.getMessage());
            }
        } catch (SQLException e) {
            LOG.error("Error getting connection: " + e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    public List<SignalScoreRow> getSignalScoreData(String signalName, long from, long to) throws SignalDataDBException {
        List<SignalScoreRow> signalScoreData = new ArrayList<>();
        String query = String.format(GET_SIGNAL_SCORE_QUERY, signalName);

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            preparedStatement.setLong(1, from);
            preparedStatement.setLong(2, to);
            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    signalScoreData.add(newSignalScoreRow(rs));
                }
            }
        }
        catch (SQLException e) {
            LOG.error("Error executing get signal score query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalScoreData;
    }

    public List<SignalScoreRowListItem> getLastSignalScoreRowsInRange(long from, long to) throws SignalDataDBException {
        List<SignalScoreRowListItem> signalScoreRows = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection()) {
            // get names of score tables
            List<String> scoreTableNames = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(GET_SIGNAL_SCORE_TABLE_NAMES_QUERY);
                 ResultSet rs = preparedStatement.executeQuery()
            ) {
                while (rs.next()) {
                    scoreTableNames.add(rs.getString(TABLE_NAME_COLUMN));
                }
            } catch (SQLException e) {
                LOG.error("Error executing get signal score table names query: " + e.getMessage());
                throw new SignalDataDBException(e);
            }

            // get last score in provided range for each score table
            for (String scoreTableName : scoreTableNames) {
                String getLastScoreInRangeQuery = String.format(GET_LAST_SCORE_IN_RANGE_QUERY, scoreTableName);
                try (PreparedStatement preparedStatement = connection.prepareStatement(getLastScoreInRangeQuery)) {
                    preparedStatement.setLong(1, from);
                    preparedStatement.setLong(2, to);
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        // max one row expected
                        if (rs.next()) {
                            String signalName = scoreTableName.substring(0, scoreTableName.indexOf(SCORE_TABLE_NAME_SUFFIX));
                            signalScoreRows.add(newSignalScoreRowListItem(signalName, rs));
                        }
                    }
                } catch (SQLException e) {
                    LOG.error("Error executing get last score in range query for table: " + e.getMessage());
                    throw new SignalDataDBException(e);
                }
            }
        } catch (SQLException e) {
            LOG.error("Error getting connection: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        return signalScoreRows;
    }

    public void saveSignalScore(String signalName, SignalScoreRow signalScoreRow) throws SignalDataDBException {
        String query = String.format(STORE_SIGNAL_SCORE_QUERY, signalName);

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)
        ){

            preparedStatement.setLong(1, signalScoreRow.getTimestampFrom());
            preparedStatement.setLong(2, signalScoreRow.getTimestampTo());
            preparedStatement.setDouble(3, signalScoreRow.getValue());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LOG.error("Error executing store signal score query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    public void saveAggregatedScore(AggregatedScoreRow aggregatedScoreRow) throws SignalDataDBException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(STORE_AGGREGATED_SCORE_QUERY)
        ){
            preparedStatement.setLong(1, aggregatedScoreRow.getTimestamp());
            preparedStatement.setDouble(2, aggregatedScoreRow.getValue());
            if (aggregatedScoreRow.getSpo2() == null) {
                preparedStatement.setNull(3, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(3, aggregatedScoreRow.getSpo2());
            }
            if (aggregatedScoreRow.getEcg() == null) {
                preparedStatement.setNull(4, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(4, aggregatedScoreRow.getEcg());
            }
            if (aggregatedScoreRow.getBp() == null) {
                preparedStatement.setNull(5, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(5, aggregatedScoreRow.getBp());
            }
            if (aggregatedScoreRow.getResp() == null) {
                preparedStatement.setNull(6, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(6, aggregatedScoreRow.getResp());
            }
            if (aggregatedScoreRow.getTemp() == null) {
                preparedStatement.setNull(7, Types.DOUBLE);
            }
            else {
                preparedStatement.setDouble(7, aggregatedScoreRow.getTemp());
            }

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LOG.error("Error executing store aggregated score query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }
    }

    public AggregatedScoreRowList getAggregatedScoreRowList(long from, long to) throws SignalDataDBException {
        List<Long> timestamp = new ArrayList<>();
        List<Double> value = new ArrayList<>();
        List<Double> spo2 = new ArrayList<>();
        List<Double> ecg = new ArrayList<>();
        List<Double> bp = new ArrayList<>();
        List<Double> resp = new ArrayList<>();
        List<Double> temp = new ArrayList<>();

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_AGGREGATED_SCORE_QUERY)
         ) {
            preparedStatement.setLong(1, from);
            preparedStatement.setLong(2, to);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    AggregatedScoreRow aggregatedScoreRow = newAggregatedScoreRow(rs);

                    timestamp.add(aggregatedScoreRow.getTimestamp());
                    value.add(aggregatedScoreRow.getValue());
                    spo2.add(aggregatedScoreRow.getSpo2());
                    ecg.add(aggregatedScoreRow.getEcg());
                    bp.add(aggregatedScoreRow.getBp());
                    resp.add(aggregatedScoreRow.getResp());
                    temp.add(aggregatedScoreRow.getTemp());
                }
            }
        }
        catch (SQLException e) {
            LOG.error("Error executing get aggregated scores query: " + e.getMessage());
            throw new SignalDataDBException(e);
        }

        return new AggregatedScoreRowList(timestamp, value, spo2, ecg, bp, resp, temp);
    }

    private SignalDataRowList newSignalDataList(String signalName, Double frequency, ResultSet rs) throws SQLException {
        List<Long> timestamps = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        while (rs.next()) {
            timestamps.add(rs.getLong(DATA_TIMESTAMP_COLUMN));
            values.add(rs.getDouble(VALUE_COLUMN));
        }

        return new SignalDataRowList(signalName, frequency, timestamps, values);
    }

    private SignalScoreRow newSignalScoreRow(ResultSet rs) throws SQLException {
        return new SignalScoreRow(rs.getLong(SCORE_FROM_COLUMN), rs.getLong(SCORE_TO_COLUMN),
                rs.getDouble(VALUE_COLUMN));
    }

    private SignalScoreRowListItem newSignalScoreRowListItem(String name, ResultSet rs) throws SQLException {
        SignalScoreRow newSignalScoreRow = newSignalScoreRow(rs);
        return new SignalScoreRowListItem(name, newSignalScoreRow);
    }

    private AggregatedScoreRow newAggregatedScoreRow(ResultSet rs) throws SQLException {
        Double spo2 = rs.getDouble(AGGREGATED_SCORE_SPO2_COLUMN);
        spo2 = rs.wasNull() ? null : spo2;
        Double ecg = rs.getDouble(AGGREGATED_SCORE_ECG_COLUMN);
        ecg = rs.wasNull() ? null : ecg;
        Double bp = rs.getDouble(AGGREGATED_SCORE_BP_COLUMN);
        bp = rs.wasNull() ? null : bp;
        Double resp = rs.getDouble(AGGREGATED_SCORE_RESP_COLUMN);
        resp = rs.wasNull() ? null : resp;
        Double temp = rs.getDouble(AGGREGATED_SCORE_TEMP_COLUMN);
        temp = rs.wasNull() ? null : temp;

        return new AggregatedScoreRow(rs.getLong(DATA_TIMESTAMP_COLUMN), rs.getDouble(VALUE_COLUMN),
                spo2, ecg, bp, resp, temp);
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
