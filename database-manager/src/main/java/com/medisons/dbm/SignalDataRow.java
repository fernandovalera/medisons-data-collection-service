package com.medisons.dbm;

public class SignalDataRow {
    private final long timestampMS;
    private final double value;

    public SignalDataRow(long timestampMS, double value) {
        this.timestampMS = timestampMS;
        this.value = value;
    }

    public long getTimestamp() {
        return timestampMS;
    }

    public double getValue() {
        return value;
    }
}
