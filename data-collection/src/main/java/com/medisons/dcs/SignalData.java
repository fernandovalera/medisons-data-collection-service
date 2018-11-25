package com.medisons.dcs;

import java.util.List;

/**
 * Specialized object to store one or more data points and metadata for a particular signal.
 */
public class SignalData {
    private final String mSignalName;
    private final Double mSignalFrequency;
    private final String mSignalTimestamp;
    private final List<Double> mDataPoints;

    /**
     * Constructor for signal data.
     * @param signalName name of the signal.
     * @param signalFrequency frequency in Hz at which the data points occur.
     * @param signalTimestamp timestamp of the first data point of the form 'YYYY.MM.DD hh:mm:ss.sss'.
     * @param dataPoints List of of one or more data points.
     */
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SignalData) {
            SignalData that = (SignalData) obj;
            return
                    this.getSignalName().equals(that.getSignalName()) &&
                    this.getSignalFrequency().equals(that.getSignalFrequency()) &&
                    this.getSignalTimestamp().equals(that.getSignalTimestamp()) &&
                    this.getDataPoints().equals(that.getDataPoints());
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return
                getSignalName() + ", " +
                getSignalFrequency() + ", " +
                getSignalTimestamp() + ", " +
                getDataPoints();
    }
}
