package com.medisons.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class VitalThread implements Runnable {

    private static final Logger LOG = Logger.getLogger(VitalThread.class.getName());

    private final String signalName;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;
    private final ThreadSocket socket;

    private ArrayList<Double> readings;
    private int readingsIndex;

    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS";  //MediCollector date format
    private static final String TERMINATION_CHAR = "|||||";

    public VitalThread(String signalName, String frequency, int dataPointsPerPacket, String dataFile,
                            ThreadSocket socket)
    {
        this.signalName = signalName;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
        this.socket = socket;

        this.readings = new ArrayList<>();
        this.readingsIndex = 0;
    }

    /**
     * Gets the readings from a text file that contains a single reading in each line,
     * parses them as doubles, and stores them in the readings array.
     */
    protected void getReadingsFromFile()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile)))
        {
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                readings.add(Double.parseDouble(line));
                line = br.readLine();
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

    /**
     * Gets the readings to include in the next packet, and returns them as a list.
     * If the end of the readings list is reached, an empty list is returned.
     * @return list of readings to include in the next packet.
     */
    protected ArrayList<Double> getReadingsForNextPacket()
    {
        ArrayList<Double> dataPoints = new ArrayList<>();
        int numDataPoints = dataPointsPerPacket;

        if (readingsIndex == readings.size())
        {
            return dataPoints;
        }

        numDataPoints = numDataPoints > (readings.size() - readingsIndex) ?
                (readings.size() - readingsIndex) : numDataPoints;

        for (int i = readingsIndex; i < readingsIndex + numDataPoints; i++)
        {
            dataPoints.add(readings.get(i));
        }

        readingsIndex += numDataPoints;
        return dataPoints;
    }

    /**
     * Converts a list of doubles to an array of bytes.
     * @param dataPoints list of doubles to convert.
     * @return a byte array representation of the list of doubles.
     */
    protected byte[] getByteArrayFromDataPoints(List<Double> dataPoints)
    {
        byte[] dataPointBytes = new byte[8 * dataPoints.size()];
        int byteIndex = 0;
        for (int i = 0; i < dataPoints.size(); i++)
        {
            byte[] dataPoint = new byte[8];
            ByteBuffer.wrap(dataPoint).order(ByteOrder.LITTLE_ENDIAN).putDouble(dataPoints.get(i));
            System.arraycopy(dataPoint, 0, dataPointBytes, byteIndex, 8);
            byteIndex += 8;
        }
        return dataPointBytes;
    }

    /**
     * Creates and returns a byte array, formatted using the MediCollector packet protocol defined
     * in https://www.medicollector.com/uploads/3/1/0/6/31064385/medicollector_bedside_-_tcp_streaming_interface.pdf
     * @return
     */
    protected byte[] preparePacket(long tZeroMillis)
    {
        StringBuilder message = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        message.append(signalName);
        message.append(frequency);
        String tZero = dateFormat.format(new Date(tZeroMillis));
        message.append(tZero);
        byte[] messageBytes = message.toString().getBytes(StandardCharsets.UTF_8);

        ArrayList<Double> dataPoints = getReadingsForNextPacket();
        if (dataPoints.isEmpty())
        {
            return null;
        }
        byte[] dataPointBytes = getByteArrayFromDataPoints(dataPoints);

        byte[] terminationBytes = TERMINATION_CHAR.getBytes(StandardCharsets.UTF_8);

        byte[] packet = new byte[messageBytes.length + dataPointBytes.length + terminationBytes.length];
        System.arraycopy(messageBytes, 0, packet, 0, messageBytes.length);
        System.arraycopy(dataPointBytes, 0, packet, messageBytes.length, dataPointBytes.length);
        System.arraycopy(terminationBytes, 0, packet,
                messageBytes.length + dataPointBytes.length, terminationBytes.length);

        LOG.info("Packet prepared with signal name: " + signalName + " frequency: " + frequency + " tZero: " +
                tZero + " datapoints: " + dataPoints.toString());

        return packet;
    }

    public void run()
    {
        getReadingsFromFile();
        int packetsSent = 0;

        try
        {
            long tZeroMillis = new Date().toInstant().toEpochMilli();
            while (true)
            {
                byte[] packet = preparePacket(tZeroMillis);
                if (packet == null)
                {
                    LOG.info(signalName.trim() + " thread finished sending packets. Sent " + packetsSent + " packets.");
                    break;
                }
                socket.write(packet);
                packetsSent++;
                tZeroMillis += 1000;

                //send packet every second
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
        catch (IOException e)
        {
            LOG.info("Error in " + signalName.trim() + " thread: " + e.getMessage());
        }

        LOG.info(signalName.trim() + " thread finished sending packets.");
    }
}
