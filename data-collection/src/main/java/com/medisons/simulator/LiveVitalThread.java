package com.medisons.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * An extension of the VitalThread class.
 *
 * Reads from a vital sign signal data file until the program is interrupted. Expects a CSV file format where the time
 * column is of the time format '.h:mm:ss a'. Sends data packet to a configured socket port.
 */
public class LiveVitalThread extends VitalThread
{
    private static final Logger LOG = Logger.getLogger(LiveVitalThread.class.getName());

    private final int timeColumn;
    private final int valueColumn;

    public LiveVitalThread(String signalName, String frequency, int dataPointsPerPacket, String dataFile,
                           ThreadSocket socket, int timeColumn, int valueColumn)
    {
        super(signalName, frequency, dataPointsPerPacket, dataFile, socket);

        this.timeColumn = timeColumn;
        this.valueColumn = valueColumn;
    }

    private static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private void sendPacket(long epochMilliseconds) throws IOException {
        byte[] packet = preparePacket(epochMilliseconds);

        if (packet == null)
        {
            LOG.info(signalName.trim() + " thread failed to prepare packet");
        }
        else
        {
            socket.write(packet);
        }

        // Clear buffer and reset readings index
        readings.clear();
        readingsIndex = 0;
    }

    public void run()
    {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("h:mm:ss a")
                .toFormatter(Locale.US);
        ZoneId zoneId = ZoneId.of("Canada/Mountain");

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            Long lastEpochMilliseconds = null;
            int numSamplesRead = 0;
            while (true)
            {
                line = br.readLine();
                if (line != null)
                {
                    String[] columns = line.split(",");
                    if (columns.length >= Integer.max(timeColumn, valueColumn) + 1 && isNumeric(columns[valueColumn]))
                    {
                        // Get 'today' by comparing the read clock time with the current clock time.
                        // If the read clock time is greater than the current clock time, assume that we are reading for
                        // the previous day.
                        Long realMillisecondsIntoDay = (long) LocalTime.now(zoneId).toSecondOfDay() * 1000;
                        Long millisecondsIntoDay = (long) LocalTime.parse(columns[timeColumn], formatter).toSecondOfDay() * 1000;
                        LocalDate today = LocalDate.now(zoneId);
                        if (millisecondsIntoDay > realMillisecondsIntoDay) {
                            today = today.minusDays(1);
                        }

                        Long millisecondsAtMidnight = today.atStartOfDay(zoneId).toEpochSecond() * 1000;

                        Long currentEpochMilliseconds = millisecondsAtMidnight + millisecondsIntoDay;

                        if (lastEpochMilliseconds == null)
                        {
                            // First time entry, mark it
                            lastEpochMilliseconds = currentEpochMilliseconds;
                        }
                        else if (numSamplesRead > 0 && !currentEpochMilliseconds.equals(lastEpochMilliseconds))
                        {
                            // If the timestamp changes before the desired number of data points are found, send the
                            // found data points and log the occurrence.
                            sendPacket(lastEpochMilliseconds);

                            lastEpochMilliseconds = currentEpochMilliseconds;
                            numSamplesRead = 0;
                            LOG.warning("Sending packet with less data points than should be sent!");
                        }

                        // Add to readings
                        Double value = Double.parseDouble(columns[valueColumn]);
                        readings.add(value);
                        numSamplesRead++;

                        if (numSamplesRead == dataPointsPerPacket) {
                            // If the desired number of data points are found, send.
                            sendPacket(lastEpochMilliseconds);
                            numSamplesRead = 0;
                        }

                        lastEpochMilliseconds = currentEpochMilliseconds;
                    }
                }
                else
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        LOG.info(signalName.trim() + " thread interrupted...");
                    }
                }
            }
        }
        catch (IOException e)
        {
            LOG.info("Error reading readings file.");
        }
        catch (NumberFormatException e)
        {
            LOG.info("Error parsing readings from file.");
        }
    }
}
