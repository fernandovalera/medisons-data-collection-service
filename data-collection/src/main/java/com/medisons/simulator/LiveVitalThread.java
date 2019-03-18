package com.medisons.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.logging.Logger;

public class LiveVitalThread extends VitalThread
{
    private static final Logger LOG = Logger.getLogger(LiveVitalThread.class.getName());

    public LiveVitalThread(String signalName, String frequency, int dataPointsPerPacket, String dataFile,
                           ThreadSocket socket)
    {
        super(signalName, frequency, dataPointsPerPacket, dataFile, socket);
    }

    private static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public void run()
    {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("h:mm:ss a")
                .toFormatter(Locale.US);

        try (BufferedReader br = new BufferedReader(new FileReader(dataFile)))
        {
            String line;
            Long lastEpochMilliseconds = null;
            while (true)
            {
                line = br.readLine();
                if (line != null)
                {
                    String[] columns = line.split(",");
                    if (columns.length == 2 && isNumeric(columns[1]))
                    {
                        LocalDate today = LocalDate.now(ZoneId.of("Canada/Mountain"));
                        Long millisecondsAtMidnight = today.atStartOfDay(ZoneId.of("Canada/Mountain")).toEpochSecond() * 1000;
                        Long millisecondsIntoDay = (long) LocalTime.parse(columns[0], formatter).toSecondOfDay() * 1000;

                        Long currentEpochMilliseconds = millisecondsAtMidnight + millisecondsIntoDay;

                        if (lastEpochMilliseconds == null)
                        {
                            // First time entry, mark it
                            lastEpochMilliseconds = currentEpochMilliseconds;
                        }
                        else if (!currentEpochMilliseconds.equals(lastEpochMilliseconds))
                        {
                            System.out.println("Preparing packet with timestamp: " + lastEpochMilliseconds.toString());
                            // New time entry, send packet for existing data and clear
                            byte[] packet = preparePacket(lastEpochMilliseconds);

                            lastEpochMilliseconds = currentEpochMilliseconds;

                            if (packet == null)
                            {
                                LOG.info(signalName.trim() + " thread failed to prepare packet");
                                continue;
                            }

                            socket.write(packet);

                            // Clear buffer and reset readings index and sample counter
                            readings.clear();
                            readingsIndex = 0;
                        }

                        // Add to readings
                        Double value = Double.parseDouble(columns[1]);
                        readings.add(value);
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
