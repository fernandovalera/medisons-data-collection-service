package com.medisons.dbm;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class SignalDataRepository {

    private static final Logger LOG = Logger.getLogger(SignalDataRepository.class.getName());

    private static final String STORE_SIGNAL_DATA_QUERY = "REPLACE INTO %s VALUES %s";
    private static final String STORE_SIGNAL_INFO_ENTRY_QUERY = "REPLACE INTO signal_info VALUES (?, ?)";
    private static final String GET_SIGNAL_DATA_QUERY = "SELECT timestampMilli, value FROM %s WHERE timestampMilli BETWEEN ? AND ? ORDER BY timestampMilli";
    private static final String GET_SIGNAL_FREQUENCY_QUERY = "SELECT frequency FROM signal_info WHERE name = ?";

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

    private void createSignalInfoEntry(String signalName, double frequency) {
        try {
            PreparedStatement statement = this.signalDataConnection.prepareStatement(STORE_SIGNAL_INFO_ENTRY_QUERY);
            statement.setString(1, signalName);
            statement.setDouble(2, frequency);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
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

        double frequency = signalData.getFrequency();

        createSignalInfoEntry(signalName, frequency);

        try {
            Calendar calendar = Calendar.getInstance();
            Date date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS").parse(signalData.getTimestamp());
            calendar.setTime(date);

            int step = 0;
            long timeInMS = calendar.getTimeInMillis();
            for (Double dataPoint : signalData.getDataPoints()) {
                timeInMS = timeInMS + (long) (step / frequency * 1000);
                dataPointsString.append("(").append(timeInMS).append(",").append(dataPoint).append("),");
                step++;
            }

            // Remove last comma
            dataPointsString.deleteCharAt(dataPointsString.length()-1);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        String query = String.format(STORE_SIGNAL_DATA_QUERY, signalName, dataPointsString);
        try {
            Statement statement = signalDataConnection.createStatement();

            statement.executeUpdate(query);
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

        String timestamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS").format(new Date(timeInMS));

        return new SignalData(signalName, frequency, timestamp, dataPoints);
    }

    private SignalDataRow newSignalDataRow(ResultSet rs) throws SQLException {
        return new SignalDataRow(rs.getLong(1), rs.getDouble(2));
    }
}
