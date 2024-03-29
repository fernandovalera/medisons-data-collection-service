package com.medisons.dbm;

public class SignalScoreRow {
    private final long timestampFrom;
    private final long timestampTo;
    private final double value;

    public SignalScoreRow(long timestampFrom, long timestampTo, double value) {
        this.timestampFrom = timestampFrom;
        this.timestampTo = timestampTo;
        this.value = value;
    }

    public long getTimestampFrom() {
        return timestampFrom;
    }

    public long getTimestampTo() {
        return timestampTo;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SignalScoreRow)) {
            return false;
        }

        SignalScoreRow other = (SignalScoreRow) object;
        return other.timestampFrom == this.timestampFrom && other.timestampTo == this.timestampTo
                && other.value == this.value;
    }
}
