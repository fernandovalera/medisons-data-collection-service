package com.medisons.simulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SensorSimulator {

    private static final Logger LOG = Logger.getLogger(SensorSimulator.class.getName());
    private static final int SENSOR_PORT = 2057;

    @Parameter(names = "-data", description = "Data directory to find vital sign signal files in.")
    private String vitalsDataDir = "resources";
    @Parameter(names = "-live", description = "Data files are written to by an external source continuously.")
    private boolean useLiveConfig = false;

    public static void main(String[] argv) {
        SensorSimulator sensorSimulator = new SensorSimulator();
        JCommander.newBuilder()
                .addObject(sensorSimulator)
                .build()
                .parse(argv);
        sensorSimulator.run();
    }

    public void run() {
        ServerSocket listener;
        try {
            listener = new ServerSocket(SENSOR_PORT);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warning("Error initializing server socket. Exiting...");
            return;
        }

        URL configFileURI;
        if (useLiveConfig) {
            configFileURI = SensorSimulator.class.getClassLoader().getResource("live_vitals.xml");
        } else {
            configFileURI = SensorSimulator.class.getClassLoader().getResource("vitals.xml");
        }

        ConfigParser configParser = new ConfigParser(Objects.requireNonNull(configFileURI).toString(), useLiveConfig);
        List<Vital> vitals = configParser.getVitalsFromConfigFile(vitalsDataDir);

        while (true) {
            try (Socket socket = listener.accept())
            {
                ThreadSocket threadSocket = new ThreadSocket(socket);
                List<Thread> vitalThreads = new ArrayList<>();
                for (Vital vital : vitals)
                {
                    if (!vital.isEnabled())
                        continue;

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
