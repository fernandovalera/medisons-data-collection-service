package com.medisons.simulator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reads from config file and returns a list of vital listeners with metadata about that vital including its data file.
 *
 * Vital metadata is parsed and formatted in a way that mimics MediCollector's output signal data format.
 */
public class ConfigParser {

    private static final Logger LOG = Logger.getLogger(ConfigParser.class.getName());

    // These config files contain the relative paths for the data files to use for each vital.
    private static final String CONFIG_FILE_PATH = "resources/vitals.xml";
    private static final String LIVE_CONFIG_FILE_PATH = "resources/live_vitals.xml";

    private static final String VITAL_PATH = "/Vitals/Vital";
    private static final String NAME_PATH = "/Vitals/Vital[%d]/Name";
    private static final String FREQUENCY_PATH = "/Vitals/Vital[%d]/Frequency";
    private static final String DATAFILE_PATH = "/Vitals/Vital[%d]/DataFile";
    private static final String TIME_COLUMN_PATH = "/Vitals/Vital[%d]/TimeColumn";
    private static final String VALUE_COLUMN_PATH = "/Vitals/Vital[%d]/ValueColumn";
    private static final String ENABLED_PATH = "/Vitals/Vital[%d]/Enabled";

    private String mConfigFilePath;
    private boolean mLiveConfig;

    /**
     * Constructs new ConfigParser.
     *
     * @param configFilePath The config file to read from.
     * @param useLiveConfig Whether the supplied config file is for live files or not.
     */
    public ConfigParser(String configFilePath, boolean useLiveConfig) {
        mConfigFilePath = configFilePath;
        mLiveConfig = useLiveConfig;
    }

    /**
     * Read from config file and parse vital metadata into Vital objects and return them as a list to the caller.
     * If reading from live config file, read additional time and value column elements.
     *
     * @param vitalsDataDir The directory to find the data files in.
     * @return List of Vital objects that contain metadata about the particular vital including its data file (path).
     */
    public List<Vital> getVitalsFromConfigFile(String vitalsDataDir)
    {
        List<Vital> vitals = new ArrayList<>();
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(mConfigFilePath);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(VITAL_PATH);
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nl.getLength(); i++) {
                StringBuilder name = new StringBuilder((String) xpath.compile(String.format(NAME_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING));
                StringBuilder frequency = new StringBuilder((String) xpath.compile(String.format(FREQUENCY_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING));
                int dataPointsPerPacket = Math.max(1, (int) Math.round(Double.parseDouble(frequency.toString())));
                String dataFile = Paths.get(vitalsDataDir, (String)xpath.compile(String.format(DATAFILE_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING)).toString();
                String enabledString = (String)xpath.compile(String.format(ENABLED_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING);
                boolean enabled = Boolean.parseBoolean(enabledString);

                // Pad signal name and frequency names for MediCollector format
                while (name.length() < 30)
                {
                    name.append(" ");
                }
                while (frequency.length() < 10)
                {
                    frequency.append(" ");
                }

                if (mLiveConfig)
                {
                    int timeColumn = Integer.parseInt((String) xpath.compile(String.format(TIME_COLUMN_PATH, i + 1))
                            .evaluate(doc, XPathConstants.STRING)) - 1;
                    int valueColumn = Integer.parseInt((String) xpath.compile(String.format(VALUE_COLUMN_PATH, i + 1))
                            .evaluate(doc, XPathConstants.STRING)) - 1;
                    vitals.add(
                            new Vital(name.toString(), frequency.toString(), dataPointsPerPacket, dataFile, enabled,
                            timeColumn, valueColumn)
                    );
                }
                else
                {
                    vitals.add(
                            new Vital(name.toString(), frequency.toString(), dataPointsPerPacket, dataFile, enabled)
                    );
                }
            }
        }
        catch (ParserConfigurationException | SAXException | XPathExpressionException | NumberFormatException | IOException e)
        {
            LOG.severe("Failed to parse vitals config file completely: " + e.getMessage());
        }

        return vitals;
    }
}
