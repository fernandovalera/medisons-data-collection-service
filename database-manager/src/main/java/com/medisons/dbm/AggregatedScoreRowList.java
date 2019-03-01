package com.medisons.dbm;

import java.util.List;

public class AggregatedScoreRowList {

    private List<Long> timestamp;
    private List<Double> value;
    private List<Double> spo2;
    private List<Double> ecg;
    private List<Double> bp;
    private List<Double> resp;
    private List<Double> temp;


    public AggregatedScoreRowList(List<Long> timestamp, List<Double> value, List<Double> spo2,
            List<Double> ecg, List<Double> bp, List<Double> resp, List<Double> temp) {
        this.timestamp = timestamp;
        this.value = value;
        this.spo2 = spo2;
        this.ecg = ecg;
        this.bp = bp;
        this.resp = resp;
        this.temp = temp;
    }

    public List<Long> getTimestamp() {
        return timestamp;
    }

    public List<Double> getValue() {
        return value;
    }

    public List<Double> getSpo2() {
        return spo2;
    }

    public List<Double> getEcg() {
        return ecg;
    }

    public List<Double> getBp() {
        return bp;
    }

    public List<Double> getResp() {
        return resp;
    }

    public List<Double> getTemp() {
        return temp;
    }
}
