package com.medisons.dcs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    /**
     * Contructs new SignalDataReader.
     *
     * @param inputStream InputStream for data.
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
    private int readToEndOfDataPacket(byte[] dataBuffer) throws IOException {
        boolean foundPacket = false;
        int endOfPacket = 0;
        int terminationCharCount = 0;

        if (dataInputStream.markSupported()) {
            dataInputStream.mark(dataBuffer.length);
        }

        // Read into the buffer until a data packet is found or the buffer is full.
        while (!foundPacket && endOfPacket < dataBuffer.length) {
            int bytesRead = dataInputStream.read(dataBuffer, 0, dataBuffer.length);

            // If nothing was read, and nothing found in the dataBuffer, exit.
            if (bytesRead == -1) {
                return -1;
            }

            // Read through buffer up to the amount of bytes read to find the termination string.
            while (endOfPacket < dataBuffer.length)
            {
                if ((char) dataBuffer[endOfPacket] == PACKET_TERMINATION_STR.charAt(terminationCharCount))
                {
                    terminationCharCount += 1;
                    if (terminationCharCount == PACKET_TERMINATION_STR.length())
                    {
                        foundPacket = true;
                        endOfPacket +=1;
                        break;
                    }
                }
                else {
                    terminationCharCount = 0;
                }
                endOfPacket += 1;
            }
        }

        if (dataInputStream.markSupported()) {
            dataInputStream.reset();
            dataInputStream.readNBytes(endOfPacket);
        }

        return foundPacket ? endOfPacket : -2;
    }

    /**
     * Creates a new SignalData from data contained on dataBuffer.
     * @param dataBuffer Byte array containing data
     * @param endOfPacket Integer index of the end of the data packet.
     * @return A new SignalData.
     */
    private SignalData parseSignalDataPacket(byte[] dataBuffer, int endOfPacket) {
        // Parse signal name, frequency, and timestamp
        StringBuilder dataString = new StringBuilder(new String(
                Arrays.copyOfRange(dataBuffer, 0, 63),
                StandardCharsets.UTF_8
        ));

        String signalName = dataString.substring(0, FIELD_SIGNAL_NAME_LENGTH).trim();
        dataString.delete(0, FIELD_SIGNAL_NAME_LENGTH);

        Double signalFrequency = Double.parseDouble(dataString.substring(0, FIELD_SIGNAL_FREQUENCY_LENGTH));
        dataString.delete(0, FIELD_SIGNAL_FREQUENCY_LENGTH);

        String signalTimestamp = dataString.substring(0, FIELD_TIMESTAMP_LENGTH).trim();

        // Convert data points to List of Doubles
        List<Double> dataPoints = new ArrayList<>();
        byte[] rawDataPoints = Arrays.copyOfRange(dataBuffer, 63, endOfPacket - 4);
        DoubleBuffer doubleBuffer = ByteBuffer.wrap(rawDataPoints).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
        while (doubleBuffer.hasRemaining())
        {
            dataPoints.add(doubleBuffer.get());
        }

        return new SignalData(signalName, signalFrequency, signalTimestamp, dataPoints);
    }

    /**
     * Retrieves one or more data packets. Blocks until at least one data packet is retrieved.
     *
     * @return List of data packets.
     * @throws IOException When end of stream is reached without reading to the end of a packet.
     */
    List<SignalData> getDataPackets() throws IOException {
        List<SignalData> dataPackets = new ArrayList<>();
        while (dataInputStream.available() > 0 || dataPackets.size() == 0) {
            byte[] dataBuffer = new byte[DATA_BUFFER_SIZE];
            int endOfPacket = readToEndOfDataPacket(dataBuffer);

            if (endOfPacket < 0) {
                throw new IOException();
            }

            SignalData dataPacket = parseSignalDataPacket(dataBuffer, endOfPacket);

            dataPackets.add(dataPacket);
        }

        return dataPackets;
    }

    public static void main(String[] args)
    {
        while (true) {
            try {
                Socket dataInSocket = new Socket("", DATA_IN_PORT);

                SignalDataReader dataReader = new SignalDataReader(new BufferedInputStream(dataInSocket.getInputStream()));

                DataDistributor dataDistributor = new DataDistributor();

                // Instead of sending data packets to the unfinished DataDistributor, log to console.
                while (true) {
                    LOG.info(dataReader.getDataPackets().toString());
                }
            } catch (IOException e) {
                LOG.info("Socket is not sending data, waiting ...");
            }

            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
