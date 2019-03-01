package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignalDataListTest {

    private final static String SIGNAL_NAME = "spo2";
    private final static long SIGNAL_TIMESTAMP_MS_1 = 100L;
    private final static long SIGNAL_TIMESTAMP_MS_2 = 200L;
    private final static long SIGNAL_TIMESTAMP_MS_3 = 300L;
    private final static double SIGNAL_VALUE_1 = 10.10d;
    private final static double SIGNAL_VALUE_2 = 13.52d;
    private final static double SIGNAL_VALUE_3 = 15.52d;


    private List<Long> expectedTimestamps;
    private List<Double> expectedValues;

    private SignalDataList signalDataList;

    @BeforeEach
    void setUp() {
        expectedTimestamps = new ArrayList<>();
        expectedTimestamps.add(SIGNAL_TIMESTAMP_MS_1);
        expectedTimestamps.add(SIGNAL_TIMESTAMP_MS_2);

        expectedValues = new ArrayList<>();
        expectedValues.add(SIGNAL_VALUE_1);
        expectedValues.add(SIGNAL_VALUE_2);

        signalDataList = new SignalDataList(SIGNAL_NAME, expectedTimestamps, expectedValues);
    }

    @AfterEach
    void tearDown() {
        expectedTimestamps = null;
        expectedValues = null;
        signalDataList = null;
    }

    @Test
    void getName() {
        assertEquals(SIGNAL_NAME, signalDataList.getName());
    }

    @Test
    void getTimestamps() {
        assertEquals(expectedTimestamps, signalDataList.getTimestamps());
    }

    @Test
    void getValues() {
        assertEquals(expectedValues, signalDataList.getValues());
    }

    @Test
    void equals_givenEqualOther_returnTrue() {
        SignalDataList otherSignalDataList = new SignalDataList(SIGNAL_NAME, expectedTimestamps, expectedValues);

        assertEquals(otherSignalDataList, signalDataList);
    }

    @Test
    void equals_givenNonEqualOther_returnFalse() {
        List<Long> expectedTimestamps = new ArrayList<>();
        expectedTimestamps.add(SIGNAL_TIMESTAMP_MS_2);
        expectedTimestamps.add(SIGNAL_TIMESTAMP_MS_3);
        List<Double> expectedValues = new ArrayList<>();
        expectedValues.add(SIGNAL_VALUE_2);
        expectedValues.add(SIGNAL_VALUE_3);

        SignalDataList otherSignalDataList = new SignalDataList(SIGNAL_NAME, expectedTimestamps, expectedValues);

        assertNotEquals(otherSignalDataList, signalDataList);
    }
}