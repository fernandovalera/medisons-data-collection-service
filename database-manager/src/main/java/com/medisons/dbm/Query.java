package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import java.util.ArrayList;
import java.util.List;

public class Query implements GraphQLQueryResolver {

    private final SignalDataRepository signalDataRepository;

    public Query(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public List<SignalData> multiSignalData(List<String> signalNames, long from, long to) throws Exception {
        List<SignalData> multiSignalDataList = new ArrayList<>();
        for (String signalName : signalNames)
        {
            multiSignalDataList.add(
                    signalDataRepository.getAllSignalData(signalName, from, to)
            );
        }
        return multiSignalDataList;
    }

    public SignalData allSignalData(String signalName, long from, long to) throws Exception {
        return signalDataRepository.getAllSignalData(signalName, from, to);
    }

    public List<SignalDataRowList> multiSignalDataRow(List<String> signalNames, long from, long to) throws Exception {
        List<SignalDataRowList> multiSignalDataRows = new ArrayList<>();
        for (String signalName : signalNames)
        {
            multiSignalDataRows.add(
                    new SignalDataRowList(signalName, signalDataRepository.getAllSignalDataRow(signalName, from, to))
            );
        }
        return multiSignalDataRows;
    }

    public List<SignalDataRow> signalDataRow(String signalName, long from, long to) throws Exception {
        return signalDataRepository.getAllSignalDataRow(signalName, from, to);
    }

    public List<SignalScoreRow> signalScoreData(String signalName, long from, long to) throws Exception {
        return signalDataRepository.getSignalScoreData(signalName, from, to);
    }
}
