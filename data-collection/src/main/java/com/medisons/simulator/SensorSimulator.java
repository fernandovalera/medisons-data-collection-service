package com.medisons.simulator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;


public class SensorSimulator {

    private static final Logger LOG = Logger.getLogger(SensorSimulator.class.getName());

    private static final String DCS_HOST = "localhost";
    private static final int DCS_PORT = 2057;

    private static final String DATA_FILE = "resources/Sample_Patient_O2_Data_ASCII.txt";

    private static final String SIGNAL_NAME = "spo2                          "; //signal name with padding to ensure 30 chars
    private static final String FREQUENCY = "125       ";                       //frequency with padding to ensure 10 chars
    private static final String DATE_FORMAT = "yyyy.MM.dd HH:mm:ss.SSS";        //MediCollector date format
    private static final int DATAPOINTS_PER_PACKET = 125;                       //max number of datapoints that can be sent in a single packet
    private static final String TERMINATION_CHAR = "|||||";

    private ArrayList<Double> o2Readings;
    private int o2ReadingsIndex;

    public SensorSimulator()
    {
        o2Readings = new ArrayList<>();
        o2ReadingsIndex = 0;
    }

    /**
     * Gets the readings from a text file that contains a single reading in each line,
     * parses them as doubles, and stores them in the o2Readings array.
     */
    protected void getReadingsFromFile()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(DATA_FILE)))
        {
            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                o2Readings.add(Double.parseDouble(line));
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
        int numDataPoints = DATAPOINTS_PER_PACKET;

        if (o2ReadingsIndex == o2Readings.size())
        {
            return dataPoints;
        }

        numDataPoints = numDataPoints > (o2Readings.size() - o2ReadingsIndex) ?
                (o2Readings.size() - o2ReadingsIndex) : numDataPoints;

        for (int i = o2ReadingsIndex; i < o2ReadingsIndex + numDataPoints; i++)
        {
            dataPoints.add(o2Readings.get(i));
        }

        o2ReadingsIndex += numDataPoints;
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

        message.append(SIGNAL_NAME);
        message.append(FREQUENCY);
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

        LOG.info("Packet prepared with signal name: " + SIGNAL_NAME + " frequency: " + FREQUENCY + " tZero: " +
                tZero + " datapoints: " + dataPoints.toString());

        return packet;
    }

    public static void main(String[] argv){

        SensorSimulator sensorSimulator = new SensorSimulator();
        sensorSimulator.getReadingsFromFile();
        int packetsSent = 0;

        try (Socket socket = new Socket(DCS_HOST, DCS_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream()))
        {
            long tZeroMillis = new Date().toInstant().toEpochMilli();
            while (true)
            {
                byte[] packet = sensorSimulator.preparePacket(tZeroMillis);
                if (packet == null)
                {
                    LOG.info("Finished sending packets. Sent " + packetsSent + " packets.");
                    break;
                }
                out.write(packet, 0, packet.length);
                out.flush();
                packetsSent++;
                tZeroMillis += 1000;

                //send packet every second
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    LOG.info("Simulation script interrupted...");
                }
            }
        }
        catch (IOException e)
        {
            LOG.info("Error: " + e.getMessage());
        }

        LOG.info("Finished sending packets, connection closed.");
    }

}
