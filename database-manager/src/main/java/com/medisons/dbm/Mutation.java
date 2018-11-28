package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import java.util.List;

public class Mutation implements GraphQLMutationResolver {

    private final SignalDataRepository signalDataRepository;

    public Mutation(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public SignalData storeSignalData(String signalName, Double frequency, String timestamp,
                                      List<Double> dataPoints) {
        SignalData newSignalData = new SignalData(signalName, frequency, timestamp, dataPoints);
        signalDataRepository.saveSignalData(signalName, newSignalData);
        return newSignalData;
    }
}
