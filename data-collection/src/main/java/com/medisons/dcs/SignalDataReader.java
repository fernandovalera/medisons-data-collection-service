package com.medisons.dcs;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reads data packets from a single input stream
 */
public class SignalDataReader {

    private static final Logger LOG = Logger.getLogger(SignalDataReader.class.getName());

    private final static int DATA_IN_PORT = 2057;
    private final static int FIELD_SIGNAL_NAME_LENGTH = 30;
    private final static int FIELD_SIGNAL_FREQUENCY_LENGTH = 10;
    private final static int FIELD_TIMESTAMP_LENGTH = 23;
    private final static String PACKET_TERMINATION_STR = "|||||";
    private final static int DATA_BUFFER_SIZE = 8000;

    private InputStream dataInputStream;

    private byte[] dataBuffer = new byte[DATA_BUFFER_SIZE];
    private int dataBufferOffset = 0;

    /**
     * Contructs new SignalDataReader.
     *
     * @param inputStream InputStream for data.
     * @throws IOException When a socket cannot be opened.
     */
    public SignalDataReader(InputStream inputStream) {
        dataInputStream = inputStream;
    }

    /**
     * Read from dataInputStream into dataBuffer until either the data buffer is full or the termination string is read.
     *
     * @return Integer value indicating the index of end of the packet in the data buffer or
     * -1 if end of stream is reached,
     * -2 if the buffer is full.
     * @throws IOException When there was an exception reading from InputStream.
     */
    protected int readToEndOfDataPacket() throws IOException {
        boolean foundPacket = false;
        int endOfPacket = 0;
        int terminationCharCount = 0;

        // Read into the buffer until a data packet is found or the buffer is full.
        while (!foundPacket && endOfPacket < dataBuffer.length) {
            int bytesRead = dataInputStream.read(dataBuffer, dataBufferOffset,
                    dataBuffer.length - dataBufferOffset);

            if (bytesRead == -1) {
                return -1;
            }

            dataBufferOffset += bytesRead;

            // Read through buffer up to the amount of bytes read to find the termination string.
            while (endOfPacket < dataBufferOffset)
            {
                if ((char) dataBuffer[endOfPacket] == PACKET_TERMINATION_STR.charAt(terminationCharCount))
                {
                    terminationCharCount += 1;
                    if (terminationCharCount == PACKET_TERMINATION_STR.length())
                    {
                        foundPacket = true;
                        break;
                    }
                }
                else {
                    terminationCharCount = 0;
                }
                endOfPacket += 1;
            }
        }

        return foundPacket ? endOfPacket : -2;
    }

    /**
     * Creates a new SignalData from data contained on dataBuffer.
     * @param endOfPacket Integer index of the end of the data packet.
     * @return A new SignalData.
     */
    protected SignalData parseSignalDataPacket(int endOfPacket) {
        // Parse signal name, frequency, and timestamp
        StringBuilder dataString = new StringBuilder(new String(
                Arrays.copyOfRange(dataBuffer, 0, 63),
                Charset.forName("UTF-8")
        ));

        String signalName = dataString.substring(0, FIELD_SIGNAL_NAME_LENGTH).trim();
        dataString.delete(0, FIELD_SIGNAL_NAME_LENGTH);

        Double signalFrequency = Double.parseDouble(dataString.substring(0, FIELD_SIGNAL_FREQUENCY_LENGTH));
        dataString.delete(0, FIELD_SIGNAL_FREQUENCY_LENGTH);

        String signalTimestamp = dataString.substring(0, FIELD_TIMESTAMP_LENGTH).trim();

        // Convert data points to List of Doubles
        List<Double> dataPoints = new ArrayList<Double>();
        byte[] rawDataPoints = Arrays.copyOfRange(dataBuffer, 63, endOfPacket - 4);
        DoubleBuffer doubleBuffer = ByteBuffer.wrap(rawDataPoints).asDoubleBuffer();
        while (doubleBuffer.hasRemaining())
        {
            dataPoints.add(doubleBuffer.get());
        }

        return new SignalData(signalName, signalFrequency, signalTimestamp, dataPoints);
    }

    /**
     * Copies bytes read over the end of the last packet to the start of the byte array and moves dataBufferOffset to
     * end of bytes read over.
     *
     * @param endOfPacket Integer index of the end of the data packet.
     */
    protected void resetDataBuffer(int endOfPacket) {
        int readBytesOver =  dataBufferOffset - endOfPacket - 1;

        if (readBytesOver > 0) {
            LOG.info(String.format("Read %d bytes over the end of the last packet.", readBytesOver));
        }

        for (int i = 0; i < readBytesOver; i++) {
            dataBuffer[i] = dataBuffer[endOfPacket + 5];
        }

        dataBufferOffset = readBytesOver;
    }

    public SignalData getDataPacket() throws IOException {
        int endOfPacket = readToEndOfDataPacket();

        if (endOfPacket < 0) {
            throw new IOException();
        }

        SignalData dataPacket = parseSignalDataPacket(endOfPacket);

        resetDataBuffer(endOfPacket);

        return dataPacket;
    }

    public static void main(String[] args)
    {
        try {
            ServerSocket listener = new ServerSocket(DATA_IN_PORT);
            Socket dataInSocket = listener.accept();
            SignalDataReader dataReader = new SignalDataReader(dataInSocket.getInputStream());

            while (true) {
                try {
                    LOG.info(dataReader.getDataPacket().toString());
                } catch (IOException e) {
                    LOG.info("Socket is not sending data, waiting ...");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            LOG.info("Error establishing connection.");
            e.printStackTrace();
        }
    }
}
