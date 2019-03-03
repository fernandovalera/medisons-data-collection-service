package com.medisons.dbm;

import java.util.List;

public class SignalDataRowList {
    private final String name;
    private final Double frequency;
    private final List<Long> timestamps;
    private final List<Double> values;

    public SignalDataRowList(String name, Double frequency, List<Long> timestamps, List<Double> values) {
        this.name = name;
        this.frequency = frequency;
        this.timestamps = timestamps;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public Double getFrequency() {
        return frequency;
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public List<Double> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SignalDataRowList)) {
            return false;
        }

        SignalDataRowList other = (SignalDataRowList) object;
        return other.name.equals(this.name) && other.timestamps.equals(this.timestamps)
                && other.values.equals(this.values);
    }
}
