package com.medisons.dbm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public class SignalDataRepository {

    private static final Logger LOG = Logger.getLogger(SignalDataRepository.class.getName());

    private static final String STORE_SIGNAL_DATA_QUERY = "REPLACE INTO %s VALUES %s";
    private static final String STORE_SIGNAL_INFO_ENTRY_QUERY = "REPLACE INTO signal_info VALUES (?, ?)";
    private static final String STORE_SIGNAL_SCORE_QUERY = "REPLACE INTO %s_score VALUE (?, ?, ?)";
    private static final String GET_SIGNAL_DATA_QUERY = "SELECT timestamp, value FROM %s WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
    private static final String GET_SIGNAL_FREQUENCY_QUERY = "SELECT frequency FROM signal_info WHERE name = ?";
    private static final String GET_SIGNAL_SCORE_QUERY = "SELECT timestampFrom, timestampTo, value FROM %s_score WHERE timestampFrom >= ? AND timestampTo <= ?"
            + " ORDER BY timestampFrom";

    private static final String DATA_TIMESTAMP_COLUMN = "timestamp";
    private static final String VALUE_COLUMN = "value";
    private static final String SCORE_FROM_COLUMN = "timestampFrom";
    private static final String SCORE_TO_COLUM = "timestampTo";

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
            e.printStackTrace();
        }
        return false;
    }

    public SignalData getAllSignalData(String signalName, long from, long to) {
        SignalData signalData = null;

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
                // TODO: Should notify client that signal name was not recognized.
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return signalData;
    }

    public List<SignalDataRow> getAllSignalDataRow(String signalName, long from, long to) {
        List<SignalDataRow> allSignalDataRow = new ArrayList<>();

        try {
            String query = String.format(GET_SIGNAL_DATA_QUERY, signalName);
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
            preparedStatement.setLong(1, from);
            preparedStatement.setLong(2, to);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                allSignalDataRow.add(newSignalDataRow(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return allSignalDataRow;
    }

    public void saveSignalData(String signalName, SignalData signalData) {
        StringBuilder dataPointsString = new StringBuilder();

        if (!tableExists(signalName)) {
            LOG.info(String.format("Could not find table for signal '%s'.", signalName));
            return;
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

        // Create and execute signal data update query
        String query = String.format(STORE_SIGNAL_DATA_QUERY, signalName, dataPointsString);
        try {
            Statement statement = signalDataConnection.createStatement();

            statement.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<SignalScoreRow> getSignalScoreData (String signalName, long from, long to) {
        List<SignalScoreRow> signalScoreData = new ArrayList<>();

        try {
            String query = String.format(GET_SIGNAL_SCORE_QUERY, signalName);
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
            preparedStatement.setLong(1, from);
            preparedStatement.setLong(2, to);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                signalScoreData.add(newSignalScoreRow(rs));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return signalScoreData;
    }

    public void saveSignalScore(String signalName, SignalScoreRow signalScoreRow) {
        try {
            String query = String.format(STORE_SIGNAL_SCORE_QUERY, signalName);
            PreparedStatement preparedStatement = signalDataConnection.prepareStatement(query);
            preparedStatement.setLong(1, signalScoreRow.getTimestampFrom());
            preparedStatement.setLong(2, signalScoreRow.getTimestampTo());
            preparedStatement.setDouble(3, signalScoreRow.getValue());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
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
        return new SignalScoreRow(rs.getLong(SCORE_FROM_COLUMN), rs.getLong(SCORE_TO_COLUM),
                rs.getDouble(VALUE_COLUMN));
    }
}
