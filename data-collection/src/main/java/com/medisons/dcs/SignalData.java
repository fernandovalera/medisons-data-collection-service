package com.medisons.dcs;

import java.util.List;

public class SignalData {
    private final String mSignalName;
    private final Double mSignalFrequency;
    private final String mSignalTimestamp;
    private final List<Double> mDataPoints;

    public SignalData(String signalName, Double signalFrequency, String signalTimestamp, List<Double> dataPoints) {
        mSignalName = signalName;
        mSignalFrequency = signalFrequency;
        mSignalTimestamp = signalTimestamp;
        mDataPoints = dataPoints;
    }

    public String getSignalName() {
        return mSignalName;
    }

    public Double getSignalFrequency() {
        return mSignalFrequency;
    }

    public String getSignalTimestamp() {
        return mSignalTimestamp;
    }

    public List<Double> getDataPoints() {
        return mDataPoints;
    }

    public String toString() {
        return getSignalName() + ", " +
                getSignalFrequency() + ", " +
                getSignalTimestamp() + ", " +
                getDataPoints();
    }
}
