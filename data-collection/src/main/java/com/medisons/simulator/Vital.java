package com.medisons.simulator;

public class Vital {

    private final String name;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;
    private final boolean enabled;

    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile, boolean enabled)
    {
        this.name = name;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
        this.enabled = enabled;
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

    public boolean isEnabled() {
        return enabled;
    }
}
