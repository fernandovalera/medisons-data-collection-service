package com.medisons.simulator;

public class Vital {

    private final String name;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;
    private final int timeColumn;
    private final int valueColumn;

    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile, int timeColumn, int valueColumn)
    {
        this.name = name;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
        this.timeColumn = timeColumn;
        this.valueColumn = valueColumn;
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

    public int getTimeColumn() {
        return timeColumn;
    }

    public int getValueColumn() {
        return valueColumn;
    }
}
