package com.medisons.dbm;

import java.util.List;

public class SignalDataList {
    private final String name;
    private final List<Long> timestamps;
    private final List<Double> values;

    public SignalDataList(String name, List<Long> timestamps, List<Double> values) {
        this.name = name;
        this.timestamps = timestamps;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public List<Double> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SignalDataList)) {
            return false;
        }

        SignalDataList other = (SignalDataList) object;
        return other.name.equals(this.name) && other.timestamps.equals(this.timestamps)
                && other.values.equals(this.values);
    }
}
