package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignalScoreRowListItemTest {

    private final static String SIGNAL_NAME = "spo2";
    private final static SignalScoreRow SIGNAL_SCORE_ROW = new SignalScoreRow(0, 100, 1.65);

    private SignalScoreRowListItem signalScoreRowListItem;

    @BeforeEach
    void setUp() {
        signalScoreRowListItem = new SignalScoreRowListItem(SIGNAL_NAME, SIGNAL_SCORE_ROW);
    }

    @AfterEach
    void tearDown() {
        signalScoreRowListItem = null;
    }

    @Test
    void getName() {
        assertEquals(SIGNAL_NAME, signalScoreRowListItem.getName());
    }

    @Test
    void getScore() {
        assertEquals(SIGNAL_SCORE_ROW, signalScoreRowListItem.getScore());
    }
}