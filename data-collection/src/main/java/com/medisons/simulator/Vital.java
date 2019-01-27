package com.medisons.simulator;

public class Vital {

    private final String name;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;

    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile)
    {
        this.name = name;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
    }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getDataPointsPerPacket() {
        return dataPointsPerPacket;
    }

    public String getDataFile() {
        return dataFile;
    }
}
