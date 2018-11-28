package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import java.util.List;

public class Query implements GraphQLQueryResolver {

    private final SignalDataRepository signalDataRepository;

    public Query(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public SignalData allSignalData(String signalName, long from, long to) {
        return signalDataRepository.getAllSignalData(signalName, from, to);
    }

    public List<SignalDataRow> signalDataRow(String signalName, long from, long to) {
        return signalDataRepository.getAllSignalDataRow(signalName, from, to);
    }

    public List<SignalScoreRow> signalScoreData(String signalName, long from, long to) {
        return signalDataRepository.getSignalScoreData(signalName, from, to);
    }
}
