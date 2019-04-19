package com.medisons.simulator;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ConfigParserTest {
    private final static String VITALS_DATA_DIR = "resources";

    @Test
    void getVitalsFromConfigFile_twoVitalsWithSpecifiedDataDir() {
        URL configFileURI = getClass().getClassLoader().getResource("test_vitals.xml");
        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(),
                false);

        List<Vital> vitalList = configParser.getVitalsFromConfigFile(VITALS_DATA_DIR);

        String expectedVitalName1 = "bp_dia";
        String expectedVitalName2 = "ecg";
        Vital expectedVital1 = new Vital(expectedVitalName1 + " ".repeat(30 - expectedVitalName1.length()),
                "1" + " ".repeat(10 - 1), 1,
                Paths.get(VITALS_DATA_DIR, "BP_DIA_ASCII.txt").toString(), true);
        Vital expectedVital2 = new Vital(expectedVitalName2 + " ".repeat(30 - expectedVitalName2.length()),
                "300" + " ".repeat(10 - 3), 300,
                Paths.get(VITALS_DATA_DIR, "ECG_ASCII.txt").toString(), false);

        assertEquals(2, vitalList.size());
        assertEquals(expectedVital1, vitalList.get(0));
        assertEquals(expectedVital2, vitalList.get(1));
    }

    @Test
    void getVitalsFromConfigFile_singleVitalWithLessThanOneHzFrequency() {
        URL configFileURI = getClass().getClassLoader().getResource("test_vitals_lessThanOneHertz.xml");
        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(),
                false);

        List<Vital> vitalList = configParser.getVitalsFromConfigFile("");

        String expectedVitalName = "bp_dia";
        Vital expectedVital = new Vital(expectedVitalName + " ".repeat(30 - expectedVitalName.length()),
                "0.2" + " ".repeat(10 - 3), 1, "BP_DIA_ASCII.txt", true);

        assertEquals(1, vitalList.size());
        assertEquals(expectedVital, vitalList.get(0));
    }

    @Test
    void getVitalsFromConfigFile_twoVitalsLive() {
        URL configFileURI = getClass().getClassLoader().getResource("test_live_vitals.xml");
        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(),
                true);

        List<Vital> vitalList = configParser.getVitalsFromConfigFile("");

        String expectedVitalName1 = "bp_dia";
        String expectedVitalName2 = "ecg";
        Vital expectedVital1 = new Vital(expectedVitalName1 + " ".repeat(30 - expectedVitalName1.length()),
                "1" + " ".repeat(10 - 1), 1, "ExportData.csv", true,
                1, 4);
        Vital expectedVital2 = new Vital(expectedVitalName2 + " ".repeat(30 - expectedVitalName2.length()),
                "300" + " ".repeat(10 - 3), 300, "ExportDataECG.csv", false,
                0, 1);

        assertEquals(2, vitalList.size());
        assertEquals(expectedVital1, vitalList.get(0));
        assertEquals(expectedVital2, vitalList.get(1));
    }

    @Test
    void getVitalsFromConfigFile_badFrequency() {
        URL configFileURI = getClass().getClassLoader().getResource("test_vitals_badFrequency.xml");
        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(),
                false);

        List<Vital> vitalList = configParser.getVitalsFromConfigFile("");

        assertEquals(0, vitalList.size());
    }

    @Test
    void getVitalsFromConfigFile_liveMissingTimeColumn() {
        URL configFileURI = getClass().getClassLoader().getResource("test_live_vitals_missingTimeColumn.xml");
        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(),
                true);

        List<Vital> vitalList = configParser.getVitalsFromConfigFile("");

        assertEquals(0, vitalList.size());
    }
}