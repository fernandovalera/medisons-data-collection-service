package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalScoreRowTest {

    private final static long TIME_STAMP_FROM = 0;
    private final static long TIME_STAMP_TO = 100;
    private final static double VALUE = 1.65;

    private SignalScoreRow signalScoreRow;

    @BeforeEach
    void setUp() {
        signalScoreRow = new SignalScoreRow(TIME_STAMP_FROM, TIME_STAMP_TO, VALUE);
    }

    @AfterEach
    void tearDown() {
        signalScoreRow = null;
    }

    @Test
    void getTimeStampFrom() {
        assertEquals(TIME_STAMP_FROM, signalScoreRow.getTimestampFrom());
    }

    @Test
    void getTimeStampTo() {
        assertEquals(TIME_STAMP_TO, signalScoreRow.getTimestampTo());
    }

    @Test
    void getValue() {
        assertEquals(VALUE, signalScoreRow.getValue());
    }
}