package com.medisons.simulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VitalTest {

    private final static String SIGNAL_NAME = "A"+ " ".repeat(30 - 1);
    private final static String SIGNAL_FREQUENCY_STRING = "0.9" + " ".repeat(10 - 3);
    private final static int DATAPOINTS_PER_PACKET = 1;
    private final static String DATA_FILE = "AS3ExportDataA.csv";
    private final static boolean ENABLED = true;
    private final static int TIME_COLUMN = 1;
    private final static int VALUE_COLUMN = 2;

    @Test
    void getName() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(SIGNAL_NAME, vital.getName());
    }

    @Test
    void getFrequency() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(SIGNAL_FREQUENCY_STRING, vital.getFrequency());
    }

    @Test
    void getDataPointsPerPacket() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(DATAPOINTS_PER_PACKET, vital.getDataPointsPerPacket());
    }

    @Test
    void getDataFile() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(DATA_FILE, vital.getDataFile());
    }

    @Test
    void isEnabled() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(ENABLED, vital.isEnabled());
    }

    @Test
    void getTimeColumn_default() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(0, vital.getTimeColumn());
    }

    @Test
    void getValueColumn_default() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED);
        assertEquals(0, vital.getValueColumn());
    }

    @Test
    void getTimeColumn() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED,
                TIME_COLUMN, VALUE_COLUMN);
        assertEquals(TIME_COLUMN, vital.getTimeColumn());
    }

    @Test
    void getValueColumn() {
        Vital vital = new Vital(SIGNAL_NAME, SIGNAL_FREQUENCY_STRING, DATAPOINTS_PER_PACKET, DATA_FILE, ENABLED,
                TIME_COLUMN, VALUE_COLUMN);
        assertEquals(VALUE_COLUMN, vital.getValueColumn());
    }
}