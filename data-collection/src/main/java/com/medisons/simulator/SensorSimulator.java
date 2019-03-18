package com.medisons.simulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class SensorSimulator {

    private static final Logger LOG = Logger.getLogger(SensorSimulator.class.getName());

    private static final String DEFAULT_VITALS_DATA_DIR = "resources";
    private static final int SENSOR_PORT = 2057;

    public static void main(String[] argv)
    {
        String vitalsDataDir = DEFAULT_VITALS_DATA_DIR;
        boolean useLiveConfig = false;

        if (argv.length >= 1) {
            vitalsDataDir = argv[0];
        }
        if (argv.length >= 2) {
            useLiveConfig = argv[1].equalsIgnoreCase("live");
        }

        ServerSocket listener;
        try {
            listener = new ServerSocket(SENSOR_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warning("Error initializing server socket. Exiting...");
            return;
        }

        ConfigParser configParser = new ConfigParser();
        List<Vital> vitals = configParser.getVitalsFromConfigFile(useLiveConfig, vitalsDataDir);

        while (true) {
            try (Socket socket = listener.accept())
            {
                ThreadSocket threadSocket = new ThreadSocket(socket);
                List<Thread> vitalThreads = new ArrayList<>();
                for (Vital vital : vitals)
                {
                    Runnable vitalRunnable;
                    if (useLiveConfig)
                    {
                        vitalRunnable = new LiveVitalThread(vital.getName(), vital.getFrequency(),
                                vital.getDataPointsPerPacket(), vital.getDataFile(), threadSocket,
                                vital.getTimeColumn(), vital.getValueColumn());
                    }
                    else
                    {
                        vitalRunnable = new VitalThread(vital.getName(), vital.getFrequency(),
                                vital.getDataPointsPerPacket(), vital.getDataFile(), threadSocket);
                    }
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
                LOG.info("Lost connection with client, will attempt to reconnect.");
            }
        }
    }

}
