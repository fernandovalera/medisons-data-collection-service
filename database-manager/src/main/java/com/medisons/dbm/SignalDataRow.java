package com.medisons.dbm;

public class SignalDataRow {
    private final long timestampMilli;
    private final double value;

    public SignalDataRow(long timestampMilli, double value) {
        this.timestampMilli = timestampMilli;
        this.value = value;
    }

    public long getTimestampMilli() {
        return timestampMilli;
    }

    public double getValue() {
        return value;
    }
}
