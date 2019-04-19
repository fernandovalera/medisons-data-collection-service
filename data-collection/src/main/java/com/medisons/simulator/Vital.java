package com.medisons.simulator;

/**
 * Metadata for a vital signal data source.
 */
public class Vital {

    private final String name;
    private final String frequency;
    private final int dataPointsPerPacket;
    private final String dataFile;
    private final boolean enabled;
    private final int timeColumn;
    private final int valueColumn;

    /**
     * Constructs new Vital.
     *
     * @param name Right padded string name of the signal.
     * @param frequency Right padded string representing frequency of the signal.
     * @param dataPointsPerPacket Integer frequency of the signal.
     * @param dataFile The relative path of the csv data file for the signal.
     * @param enabled Whether this vital should be run or not in a vital thread.
     * @param timeColumn The column in the csv data file to parse for time.
     * @param valueColumn The column in the csv data file to parse for values.
     */
    public Vital(String name, String frequency, int dataPointsPerPacket, String dataFile, boolean enabled,
                 int timeColumn, int valueColumn)
    {
        this.name = name;
        this.frequency = frequency;
        this.dataPointsPerPacket = dataPointsPerPacket;
        this.dataFile = dataFile;
        this.enabled = enabled;
        this.timeColumn = timeColumn;
        this.valueColumn = valueColumn;
    }

    /**
     * Constructs new Vital without interest in custom time or value columns.
     *
     * @param name Right padded string name of the signal.
     * @param frequency Right padded string representing frequency of the signal.
     * @param dataPointsPerPacket Integer frequency of the signal.
     * @param dataFile The relative path of the csv data file for the signal.
     * @param enabled Whether this vital should be run or not in a vital thread.
     */
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Vital)) {
            return false;
        }

        Vital other = (Vital) object;
        return other.name.equals(this.name) && other.frequency.equals(this.frequency)
                && other.dataPointsPerPacket == this.dataPointsPerPacket && other.dataFile.equals(this.dataFile)
                && other.enabled == this.enabled && other.timeColumn == this.timeColumn
                && other.valueColumn == this.valueColumn;
    }
}
