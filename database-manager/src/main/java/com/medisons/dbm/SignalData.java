package com.medisons.dbm;

import java.util.List;

public class SignalData {
    private final String name;
    private final Double frequency;
    private final String timestamp;
    private final List<Double> dataPoints;

    public SignalData(String name, Double frequency, String timestamp, List<Double> dataPoints) {
        this.name = name;
        this.frequency = frequency;
        this.timestamp = timestamp;
        this.dataPoints = dataPoints;
    }

    public String getName() {
        return name;
    }

    public Double getFrequency() {
        return frequency;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<Double> getDataPoints() {
        return dataPoints;
    }

    @Override
    public String toString() {
        return getName() + ", " +
                getFrequency() + ", " +
                getTimestamp() + ", " +
                getDataPoints();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SignalData)) {
            return false;
        }
        else {
            SignalData other = (SignalData) object;

            return other.getName().equals(name) && other.getFrequency().equals(frequency)
                    && other.getTimestamp().equals(timestamp) && other.getDataPoints().equals(dataPoints);
        }

    }
}
