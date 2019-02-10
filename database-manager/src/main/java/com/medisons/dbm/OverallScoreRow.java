package com.medisons.dbm;

public class OverallScoreRow {

    private final long timestamp;
    private final double value;
    private final Double spo2;
    private final Double ecg;
    private final Double resp;
    private final Double temp;

    public OverallScoreRow(long timestamp, double value, Double spo2, Double ecg, Double resp, Double temp) {
        this.timestamp = timestamp;
        this.value = value;
        this.spo2 = spo2;
        this.ecg = ecg;
        this.resp = resp;
        this.temp = temp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }

    public Double getSpo2() {
        return spo2;
    }

    public Double getEcg() {
        return ecg;
    }

    public Double getResp() {
        return resp;
    }

    public Double getTemp() {
        return temp;
    }
}
