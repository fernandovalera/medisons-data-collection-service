package com.medisons.dcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SignalDataReaderTest {

    private InputStream mockInputStream;

    @BeforeEach
    void setUp() {
        mockInputStream = new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    @AfterEach
    void tearDown() {
        mockInputStream = null;
    }

    @Test
    void ctor_givenMockInputStream_success() {
        new SignalDataReader(mockInputStream);
    }

    @Test
    void getDataPacket_givenEndOfDataPacketNegative_throwException() {
        SignalDataReader dataReader = new SignalDataReader(mockInputStream) {
            protected int readToEndOfDataPacket() {
                return -1;
            }
        };

        assertThrows(IOException.class, dataReader::getDataPacket);
    }

    @Test
    void getDataPacket_givenValidDataPacket_returnSignalDataPacket() {
        SignalData mockSignalData = new SignalData(
                "a",1d,"2018.01.01 00:00:00.000", new ArrayList<>());

        SignalDataReader dataReader = new SignalDataReader(mockInputStream) {
            protected int readToEndOfDataPacket() {
                return 1;
            }

            protected SignalData parseSignalDataPacket(int endOfPacket) {
                return mockSignalData;
            }

            protected void resetDataBuffer(int endOfPacket) { }
        };

        try {
            assertEquals(mockSignalData, dataReader.getDataPacket());
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void getDataPacket_inputStream_Parsed_Correctly() throws IOException {
        byte[] packet = getTestPacket();
        mockInputStream = new ByteArrayInputStream(packet);

        SignalDataReader dataReader = new SignalDataReader(mockInputStream);

        ArrayList<Double> expectedDataPoints = new ArrayList<>();
        expectedDataPoints.add(101.5);
        expectedDataPoints.add(14.6);
        expectedDataPoints.add(63.1);
        SignalData expectedSignal = new SignalData("PulseOximetry", 1.0,
                "2017.09.02 14:39:17.863", expectedDataPoints);

        SignalData actualSignal = dataReader.getDataPacket();
        assertEquals(expectedSignal.toString(), actualSignal.toString());
    }

    private byte[] getTestPacket() {
        StringBuilder s = new StringBuilder();
        String signalName = "PulseOximetry                 ";
        String frequency = "1.000000  ";
        String tZero = "2017.09.02 14:39:17.863";
        s.append(signalName);
        s.append(frequency);
        s.append(tZero);
        byte[] messageBytes = s.toString().getBytes(StandardCharsets.UTF_8);

        ArrayList<Double> dataPoints = new ArrayList<>();
        dataPoints.add(101.5);
        dataPoints.add(14.6);
        dataPoints.add(63.1);
        byte[] dataPointBytes = new byte[8 * dataPoints.size()];
        int byteIndex = 0;
        for (int i = 0; i < dataPoints.size(); i++)
        {
            byte[] dataPoint = new byte[8];
            ByteBuffer.wrap(dataPoint).putDouble(dataPoints.get(i));
            System.arraycopy(dataPoint, 0, dataPointBytes, byteIndex, 8);
            byteIndex += 8;
        }

        byte[] terminationBytes = "|||||".getBytes(StandardCharsets.UTF_8);

        byte[] packet = new byte[messageBytes.length + dataPointBytes.length + terminationBytes.length];
        System.arraycopy(messageBytes, 0, packet, 0, messageBytes.length);
        System.arraycopy(dataPointBytes, 0, packet, messageBytes.length, dataPointBytes.length);
        System.arraycopy(terminationBytes, 0, packet,
                messageBytes.length + dataPointBytes.length, terminationBytes.length);
        return packet;
    }
}