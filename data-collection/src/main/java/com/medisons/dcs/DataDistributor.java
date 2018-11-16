package com.medisons.dcs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class DataDistributor {

    private HttpClient mClient;

    public DataDistributor() {
        mClient = HttpClient.newHttpClient();
    }

    public void storeDataPoints(SignalData signalData) {
        String body = String.format(
                "{\n storeDataPoint(signalName: %s, signalFrequency: %s, signalTimestamp: %s, dataPoints: %s)\n}",
                signalData.getSignalName(),
                signalData.getSignalFrequency(),
                signalData.getSignalTimestamp(),
                signalData.getDataPoints()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1/graphql"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        mClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }
}
