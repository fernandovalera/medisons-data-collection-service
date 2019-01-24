package com.medisons.simulator;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class SensorSimulator {

    private static final Logger LOG = Logger.getLogger(SensorSimulator.class.getName());

    private static final String DCS_HOST = "localhost";
    private static final int DCS_PORT = 2057;

    public static void main(String[] argv)
    {
        try (Socket socket = new Socket(DCS_HOST, DCS_PORT))
        {
            ThreadSocket threadSocket = new ThreadSocket(socket);
            ConfigParser configParser = new ConfigParser();
            List<Thread> vitalThreads = new ArrayList<>();
            for (Vital vital : configParser.getVitalsFromConfigFile())
            {
                Runnable vitalRunnable = new VitalThread(vital.getName(), vital.getFrequency(),
                        vital.getDataPointsPerPacket(), vital.getDataFile(), threadSocket);
                vitalThreads.add(new Thread(vitalRunnable));
            }

            for (Thread vitalThread : vitalThreads)
            {
                vitalThread.start();
            }

            for (Thread vitalThread : vitalThreads)
            {
                vitalThread.join();
            }
        }
        catch (IOException | InterruptedException e)
        {
            LOG.info("Error: " + e.getMessage());
        }
    }

}
