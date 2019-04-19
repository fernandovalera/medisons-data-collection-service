package com.medisons.dcs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Stores signal data to a GraphQL server.
 */
public class DataDistributor {

    private static final Logger LOG = Logger.getLogger(DataDistributor.class.getName());

    private static final String DBM_URI = "http://127.0.0.1:8080/database-manager/graphql";

    private HttpClient client;

    /**
     * Constructs new DataDistributor.
     *
     * @param client HttpClient for sending store request.
     */
    public DataDistributor(HttpClient client) {
        this.client = client;
    }

    /**
     * Forms and sends HTTP request to GraphQL server using provided signal data.
     *
     * @param signalData SignalData to be stored.
     * @return 0 if there were no exceptions and the HTTP response status code was 200, or
     * -1 if there was an exception,
     * the HTTP response status code otherwise.
     */
    public int storeSignalData(SignalData signalData) {
        String body = String.format(
                    "{\n" +
                    "  \"query\": \"mutation storeSignalData($name: String!, $frequency: Float!, $timestamp: String!, $dataPoints: [Float!]!) { storeSignalData(name: $name, frequency: $frequency, timestamp: $timestamp, dataPoints: $dataPoints) { name, frequency, timestamp, dataPoints } }\",\n" +
                    "  \"variables\": {\"name\": \"%s\", \"frequency\": %s, \"timestamp\": \"%s\", \"dataPoints\": %s }\n" +
                    "}",
                signalData.getSignalName(),
                signalData.getSignalFrequency(),
                signalData.getSignalTimestamp(),
                signalData.getDataPoints()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DBM_URI))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse response = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return 0;
            } else {
                return response.statusCode();
            }
        } catch (IOException | InterruptedException e) {
            LOG.warning("Failed to store signal data, unexpected error.");

            return -1;
        }
    }
}
