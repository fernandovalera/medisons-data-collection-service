package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AggregatedScoreRowTest {

    private final static long TIMESTAMP_MS = 100L;
    private final static double VALUE = 5.0;
    private final static Double SPO2 = 3.0;
    private final static Double ECG = 0.3;
    private final static Double BP = 0.1;
    private final static Double RESP = 1.0;
    private final static Double TEMP = 0.6;

    private AggregatedScoreRow aggregatedScoreRow;

    @BeforeEach
    void setUp() {
        aggregatedScoreRow = new AggregatedScoreRow(TIMESTAMP_MS, VALUE, SPO2, ECG, BP, RESP, TEMP);
    }

    @AfterEach
    void tearDown() {
        aggregatedScoreRow = null;
    }

    @Test
    void getTimeStamp() {
        assertEquals(TIMESTAMP_MS, aggregatedScoreRow.getTimestamp());
    }

    @Test
    void getValue() {
        assertEquals(VALUE, aggregatedScoreRow.getValue());
    }

    @Test
    void getSpo2() {
        assertEquals(SPO2, aggregatedScoreRow.getSpo2());
    }

    @Test
    void getEcg() {
        assertEquals(ECG, aggregatedScoreRow.getEcg());
    }

    @Test
    void getBp() {
        assertEquals(BP, aggregatedScoreRow.getBp());
    }

    @Test
    void getResp() {
        assertEquals(RESP, aggregatedScoreRow.getResp());
    }

    @Test
    void getTemp() {
        assertEquals(TEMP, aggregatedScoreRow.getTemp());
    }

    @Test
    void getComponents_areNullable() {
        aggregatedScoreRow = new AggregatedScoreRow(TIMESTAMP_MS, VALUE, null,
                null, null, null, null);
        assertEquals(TIMESTAMP_MS, aggregatedScoreRow.getTimestamp());
        assertEquals(VALUE, aggregatedScoreRow.getValue());
        assertNull(aggregatedScoreRow.getSpo2());
        assertNull(aggregatedScoreRow.getEcg());
        assertNull(aggregatedScoreRow.getBp());
        assertNull(aggregatedScoreRow.getResp());
        assertNull(aggregatedScoreRow.getTemp());
    }
}
