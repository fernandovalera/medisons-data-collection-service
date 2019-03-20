package com.medisons.simulator;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConfigParser {

    private static final Logger LOG = Logger.getLogger(ConfigParser.class.getName());
    private static final String CONFIG_FILE_PATH = "resources/vitals.xml";

    private static final String VITAL_PATH = "/Vitals/Vital";
    private static final String NAME_PATH = "/Vitals/Vital[%d]/Name";
    private static final String FREQUENCY_PATH = "/Vitals/Vital[%d]/Frequency";
    private static final String DATAFILE_PATH = "/Vitals/Vital[%d]/DataFile";
    private static final String ENABLED_PATH = "/Vitals/Vital[%d]/Enabled";

    public List<Vital> getVitalsFromConfigFile()
    {
        List<Vital> vitals = new ArrayList<>();
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(CONFIG_FILE_PATH);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile(VITAL_PATH);
            NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nl.getLength(); i++) {
                String name = (String)xpath.compile(String.format(NAME_PATH, i + 1)).evaluate(doc, XPathConstants.STRING);
                String frequency = (String)xpath.compile(String.format(FREQUENCY_PATH, i + 1)).evaluate(doc, XPathConstants.STRING);
                int dataPointsPerPacket = Integer.parseInt(frequency);
                String dataFile = (String)xpath.compile(String.format(DATAFILE_PATH, i + 1)).evaluate(doc, XPathConstants.STRING);
                String enabledString = (String)xpath.compile(String.format(ENABLED_PATH, i + 1)).evaluate(doc, XPathConstants.STRING);
                boolean enabled = Boolean.parseBoolean(enabledString);

                // pad signal name and frequency names for MediCollector format
                while (name.length() < 30)
                {
                    name += " ";
                }
                while (frequency.length() < 10)
                {
                    frequency += " ";
                }

                vitals.add(new Vital(name, frequency, dataPointsPerPacket, dataFile, enabled));
            }
        }
        catch (ParserConfigurationException | SAXException | XPathExpressionException | NumberFormatException | IOException e)
        {
            LOG.info(e.getMessage());
        }

        return vitals;
    }
}
