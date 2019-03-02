package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AggregatedScoreRowListTest {

    private static ArrayList<Long> TIMESTAMP_MS = new ArrayList<>();
    private static ArrayList<Double> VALUE = new ArrayList<>();
    private static ArrayList<Double> SPO2 = new ArrayList<>();
    private static ArrayList<Double> ECG = new ArrayList<>();
    private static ArrayList<Double> BP = new ArrayList<>();
    private static ArrayList<Double> RESP = new ArrayList<>();
    private static ArrayList<Double> TEMP = new ArrayList<>();

    private AggregatedScoreRowList aggregatedScoreRowList;

    @BeforeEach
    void setUp() {
        TIMESTAMP_MS.add(100L);
        TIMESTAMP_MS.add(200L);
        VALUE.add(5.0);
        VALUE.add(7.5);
        SPO2.add(1.0);
        SPO2.add(3.0);
        ECG.add(1.0);
        ECG.add(2.0);
        BP.add(1.0);
        BP.add(2.5);
        RESP.add(1.0);
        RESP.add(null);
        TEMP.add(1.0);
        TEMP.add(null);
        aggregatedScoreRowList = new AggregatedScoreRowList(TIMESTAMP_MS, VALUE, SPO2, ECG, BP, RESP, TEMP);
    }

    @AfterEach
    void tearDown() {
        TIMESTAMP_MS.clear();
        VALUE.clear();
        SPO2.clear();
        ECG.clear();
        BP.clear();
        RESP.clear();
        TEMP.clear();
        aggregatedScoreRowList = null;
    }

    @Test
    void getTimeStamps() {
        assertEquals(2, aggregatedScoreRowList.getTimestamp().size());
        assertEquals(100L, (long)aggregatedScoreRowList.getTimestamp().get(0));
        assertEquals(200L, (long)aggregatedScoreRowList.getTimestamp().get(1));
    }

    @Test
    void getValues() {
        assertEquals(2, aggregatedScoreRowList.getValue().size());
        assertEquals(5.0, (double)aggregatedScoreRowList.getValue().get(0));
        assertEquals(7.5, (double)aggregatedScoreRowList.getValue().get(1));
    }

    @Test
    void getSpo2() {
        assertEquals(2, aggregatedScoreRowList.getSpo2().size());
        assertEquals(1.0, (double)aggregatedScoreRowList.getSpo2().get(0));
        assertEquals(3.0, (double)aggregatedScoreRowList.getSpo2().get(1));
    }

    @Test
    void getEcg() {
        assertEquals(2, aggregatedScoreRowList.getEcg().size());
        assertEquals(1.0, (double)aggregatedScoreRowList.getEcg().get(0));
        assertEquals(2.0, (double)aggregatedScoreRowList.getEcg().get(1));
    }

    @Test
    void getBp() {
        assertEquals(2, aggregatedScoreRowList.getBp().size());
        assertEquals(1.0, (double)aggregatedScoreRowList.getBp().get(0));
        assertEquals(2.5, (double)aggregatedScoreRowList.getBp().get(1));
    }

    @Test
    void getResp() {
        assertEquals(2, aggregatedScoreRowList.getResp().size());
        assertEquals(1.0, (double)aggregatedScoreRowList.getResp().get(0));
        assertNull(aggregatedScoreRowList.getResp().get(1));
    }

    @Test
    void getTemp() {
        assertEquals(2, aggregatedScoreRowList.getTemp().size());
        assertEquals(1.0, (double)aggregatedScoreRowList.getTemp().get(0));
        assertNull(aggregatedScoreRowList.getTemp().get(1));
    }
}
