package com.medisons.simulator;

public class Vital {

    private final String name;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;
    private final boolean enabled;
    private final int timeColumn;
    private final int valueColumn;

    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile, boolean enabled, int timeColumn, int valueColumn)
    {
        this.name = name;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
        this.enabled = enabled;
        this.timeColumn = timeColumn;
        this.valueColumn = valueColumn;
    }

    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile, boolean enabled) {
        this(name, frequency, dataPointsPerPacket, dataFile, enabled, 0, 0);
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

    public int getTimeColumn() {
        return timeColumn;
    }

    public int getValueColumn() {
        return valueColumn;
    }
}
