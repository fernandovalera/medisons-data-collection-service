package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalDataTest {

    private final static String SIGNAL_NAME = "a";
    private final static Double SIGNAL_FREQUENCY = 1d;
    private final static String SIGNAL_TIMESTAMP = "2018.01.01 00:00:00.000";
    private final static List<Double> DATA_POINTS = Arrays.asList(1.38, 2.56, 4.3);

    private SignalData signalData;

    @BeforeEach
    void setUp() {
        signalData = new SignalData(SIGNAL_NAME, SIGNAL_FREQUENCY, SIGNAL_TIMESTAMP, DATA_POINTS);
    }

    @AfterEach
    void tearDown() {
        signalData = null;
    }

    @Test
    void getName_givenSignalName_returnName() {
        assertEquals(SIGNAL_NAME, signalData.getName());
    }

    @Test
    void getFrequency_givenInitializedObject_returnFrequency() {
        assertEquals(SIGNAL_FREQUENCY, signalData.getFrequency());
    }

    @Test
    void getTimestamp_givenInitializedObject_returnTimestamp() {
        assertEquals(SIGNAL_TIMESTAMP, signalData.getTimestamp());
    }

    @Test
    void getDataPoints_givenInitializedObject_returnDataPoints() {
        assertEquals(DATA_POINTS, signalData.getDataPoints());
    }
}