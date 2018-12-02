package com.medisons.dcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SignalDataTest {

    private final static String SIGNAL_NAME = "a";
    private final static Double SIGNAL_FREQUENCY = 1d;
    private final static String SIGNAL_TIMESTAMP = "2018.01.01 00:00:00.000";
    private final static List<Double> DATA_POINTS = Arrays.asList(1.38, 2.56, 4.3);

    private SignalData signalData;

    @BeforeEach
    void setUp() {
        signalData = new SignalData(SIGNAL_NAME, SIGNAL_FREQUENCY, SIGNAL_TIMESTAMP,
                DATA_POINTS);
    }

    @AfterEach
    void tearDown() {
        signalData = null;
    }

    @Test
    void getSignalName_givenSignalName_returnSignalName() {
        assertEquals(SIGNAL_NAME, signalData.getSignalName());
    }

    @Test
    void getSignalFrequency_givenSignalFrequency_returnSignalFrequency() {
        assertEquals(SIGNAL_FREQUENCY, signalData.getSignalFrequency());
    }

    @Test
    void getSignalTimestamp_givenSignalTimestamp_returnSignalTimestamp() {
        assertEquals(SIGNAL_TIMESTAMP, signalData.getSignalTimestamp());
    }

    @Test
    void getDataPoints_givenDataPoints_returnDataPoints() {
        assertEquals(DATA_POINTS, signalData.getDataPoints());
    }

    @Test
    void equals_givenPOJO_returnFalse() {
        assertNotEquals(signalData, new Object());
    }

    @Test
    void toString_returnAppropriateString() {
        assertEquals(
                SIGNAL_NAME + ", " + SIGNAL_FREQUENCY + ", " + SIGNAL_TIMESTAMP + ", " + DATA_POINTS,
                signalData.toString()
        );
    }
}