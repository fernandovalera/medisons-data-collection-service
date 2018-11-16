package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;

import java.util.List;

public class Mutation implements GraphQLMutationResolver {

    private final SignalDataRepository signalDataRepository;

    public Mutation(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public SignalData storeSignalData(String name, Double frequency, String timestamp,
                                      List<Double> dataPoints) {
        SignalData newSignalData = new SignalData(name, frequency, timestamp, dataPoints);
        signalDataRepository.saveSignalData(newSignalData);
        return newSignalData;
    }
}
