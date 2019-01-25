package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalDataRowListTest {

    private final static String SIGNAL_NAME = "spo2";
    private final static List<SignalDataRow> SIGNAL_DATA_ROWS = new ArrayList<>();

    private SignalDataRowList signalDataRowList;

    @BeforeEach
    void setUp() {
        signalDataRowList = new SignalDataRowList(SIGNAL_NAME, SIGNAL_DATA_ROWS);
    }

    @AfterEach
    void tearDown() {
        signalDataRowList = null;
    }

    @Test
    void getName() {
        assertEquals(SIGNAL_NAME, signalDataRowList.getName());
    }

    @Test
    void getRows() {
        assertEquals(SIGNAL_DATA_ROWS, signalDataRowList.getRows());
    }
}