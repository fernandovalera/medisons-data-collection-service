package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalDataRowTest {

    private final static long SIGNAL_TIMESTAMP_MS = 100L;
    private final static double SIGNAL_VALUE = 10.10d;

    private SignalDataRow signalDataRow;

    @BeforeEach
    void setUp() {
        signalDataRow = new SignalDataRow(SIGNAL_TIMESTAMP_MS, SIGNAL_VALUE);
    }

    @AfterEach
    void tearDown() {
        signalDataRow = null;
    }

    @Test
    void getTimestampMS_givenInitializedObject_returnTimestamp() {
        assertEquals(SIGNAL_TIMESTAMP_MS, signalDataRow.getTimestampMilli());
    }

    @Test
    void getValue_givenInitializedObject_returnValue() {
        assertEquals(SIGNAL_VALUE, signalDataRow.getValue());
    }
}