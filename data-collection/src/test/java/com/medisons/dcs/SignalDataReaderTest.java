package com.medisons.dcs;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignalDataReaderTest {

    private final static String SIGNAL_DATA_TEMPLATE_START = "%1$-30s%2$-10f%3$-23s";
    private final static ByteOrder SIGNAL_DATA_DATA_POINTS_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private final static String SIGNAL_DATA_TEMPLATE_END = "|||||";

    private final static String SIGNAL_DATA_NAME_1 = "Aa";
    private final static String SIGNAL_DATA_NAME_2 = "Bb";
    private final static double SIGNAL_DATA_FREQUENCY_1 = 0.9;
    private final static double SIGNAL_DATA_FREQUENCY_2 = 1.1;
    private final static String SIGNAL_DATA_TIMESTAMP_1 = "2018.01.01 00:00:00.000";
    private final static String SIGNAL_DATA_TIMESTAMP_2 = "2020.02.02 11:11:11.111";
    private final static List<Double> SIGNAL_DATA_DATA_POINTS_1 = Arrays.asList(1.05, 1.10, 1.15);
    private final static List<Double> SIGNAL_DATA_DATA_POINTS_2 = Arrays.asList(15.2, 16.8, 19.3);

    private byte[] getSignalDataByteBuffer(String name, double frequency, String timestamp, List<Double> dataPoints) {
        byte[] bufferStart = String.format(SIGNAL_DATA_TEMPLATE_START, name, frequency, timestamp)
                .getBytes(StandardCharsets.UTF_8);
        byte[] bufferEnding = SIGNAL_DATA_TEMPLATE_END.getBytes(StandardCharsets.UTF_8);
        int bufferSize = bufferStart.length + Double.BYTES * dataPoints.size() + bufferEnding.length;

        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize).put(bufferStart)
                .order(SIGNAL_DATA_DATA_POINTS_BYTE_ORDER);

        for (double d : dataPoints) {
            byteBuffer.putDouble(d);
        }

        return byteBuffer.order(ByteOrder.BIG_ENDIAN).put(bufferEnding).array();
    }

    @Test
    void ctor_givenMockInputStream_success() {
        new SignalDataReader(InputStream.nullInputStream());
    }


    @Test
    void getDataPacket_givenEmptyInputStream_throwException() {
        SignalDataReader reader = new SignalDataReader(ByteArrayInputStream.nullInputStream());

        assertThrows(IOException.class, reader::getDataPackets);
    }

    @Test
    void getDataPacket_givenIncompletePacket_throwException() {
        byte[] completePacket = getSignalDataByteBuffer(SIGNAL_DATA_NAME_1,
                SIGNAL_DATA_FREQUENCY_1,SIGNAL_DATA_TIMESTAMP_1, SIGNAL_DATA_DATA_POINTS_1);
        byte[] incompletePacket = Arrays.copyOfRange(completePacket, 0, completePacket.length - 1);

        SignalDataReader reader = new SignalDataReader(new ByteArrayInputStream(incompletePacket));

        assertThrows(IOException.class, reader::getDataPackets);
    }

    @Test
    void getDataPacket_givenValidDataPacket_returnSignalDataPacket() {
        SignalData expectedSignalData = new SignalData(SIGNAL_DATA_NAME_1, SIGNAL_DATA_FREQUENCY_1,
                SIGNAL_DATA_TIMESTAMP_1, SIGNAL_DATA_DATA_POINTS_1);

        byte[] completePacket = getSignalDataByteBuffer(SIGNAL_DATA_NAME_1, SIGNAL_DATA_FREQUENCY_1,
                SIGNAL_DATA_TIMESTAMP_1, SIGNAL_DATA_DATA_POINTS_1);

        SignalDataReader reader = new SignalDataReader(new ByteArrayInputStream(completePacket));

        try {
            SignalData actualSignalData = reader.getDataPackets().get(0);

            assertEquals(expectedSignalData, actualSignalData);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void getDataPacket_givenTwoValidDataPackets_returnSignalDataPacketsInCorrectOrder() {
        SignalData expectedSignalData1 = new SignalData(SIGNAL_DATA_NAME_1, SIGNAL_DATA_FREQUENCY_1,
                SIGNAL_DATA_TIMESTAMP_1, SIGNAL_DATA_DATA_POINTS_1);
        SignalData expectedSignalData2 = new SignalData(SIGNAL_DATA_NAME_2, SIGNAL_DATA_FREQUENCY_2,
                SIGNAL_DATA_TIMESTAMP_2, SIGNAL_DATA_DATA_POINTS_2);

        byte[] completePacket1 = getSignalDataByteBuffer(SIGNAL_DATA_NAME_1, SIGNAL_DATA_FREQUENCY_1,
                SIGNAL_DATA_TIMESTAMP_1, SIGNAL_DATA_DATA_POINTS_1);
        byte[] completePacket2 = getSignalDataByteBuffer(SIGNAL_DATA_NAME_2, SIGNAL_DATA_FREQUENCY_2,
                SIGNAL_DATA_TIMESTAMP_2, SIGNAL_DATA_DATA_POINTS_2);
        byte[] completePacket = ByteBuffer.allocate(completePacket1.length + completePacket2.length)
                .put(completePacket1).put(completePacket2)
                .array();

        SignalDataReader reader = new SignalDataReader(new ByteArrayInputStream(completePacket));

        try {
            List<SignalData> actualSignalDataList = reader.getDataPackets();

            assertEquals(expectedSignalData1, actualSignalDataList.get(0));
            assertEquals(expectedSignalData2, actualSignalDataList.get(1));
        } catch (IOException e) {
            fail();
        }
    }
}