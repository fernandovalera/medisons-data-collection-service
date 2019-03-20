package com.medisons.simulator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConfigParser {

    private static final Logger LOG = Logger.getLogger(ConfigParser.class.getName());
    private static final String CONFIG_FILE_PATH = "resources/vitals.xml";
    private static final String LIVE_CONFIG_FILE_PATH = "resources/live_vitals.xml";

    private static final String VITAL_PATH = "/Vitals/Vital";
    private static final String NAME_PATH = "/Vitals/Vital[%d]/Name";
    private static final String FREQUENCY_PATH = "/Vitals/Vital[%d]/Frequency";
    private static final String DATAFILE_PATH = "/Vitals/Vital[%d]/DataFile";
    private static final String TIME_COLUMN_PATH = "/Vitals/Vital[%d]/TimeColumn";
    private static final String VALUE_COLUMN_PATH = "/Vitals/Vital[%d]/ValueColumn";
    private static final String ENABLED_PATH = "/Vitals/Vital[%d]/Enabled";

    public List<Vital> getVitalsFromConfigFile(boolean useLiveConfig, String vitalsDataDir)
    {
        List<Vital> vitals = new ArrayList<>();
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(useLiveConfig ? LIVE_CONFIG_FILE_PATH : CONFIG_FILE_PATH);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(VITAL_PATH);
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nl.getLength(); i++) {
                StringBuilder name = new StringBuilder((String) xpath.compile(String.format(NAME_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING));
                StringBuilder frequency = new StringBuilder((String) xpath.compile(String.format(FREQUENCY_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING));
                int dataPointsPerPacket = Integer.parseInt(frequency.toString());
                String dataFile = Paths.get(vitalsDataDir, (String)xpath.compile(String.format(DATAFILE_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING)).toString();
                String enabledString = (String)xpath.compile(String.format(ENABLED_PATH, i + 1))
                        .evaluate(doc, XPathConstants.STRING);
                boolean enabled = Boolean.parseBoolean(enabledString);

                // pad signal name and frequency names for MediCollector format
                while (name.length() < 30)
                {
                    name.append(" ");
                }
                while (frequency.length() < 10)
                {
                    frequency.append(" ");
                }

                if (useLiveConfig)
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
            LOG.info(e.getMessage());
        }

        return vitals;
    }
}
