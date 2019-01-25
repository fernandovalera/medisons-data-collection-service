package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.medisons.dbm.com.medisons.dbm.SignalDataRowList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query implements GraphQLQueryResolver {

    private final SignalDataRepository signalDataRepository;

    public Query(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public SignalData allSignalData(String signalName, long from, long to) {
        return signalDataRepository.getAllSignalData(signalName, from, to);
    }

    public List<SignalDataRowList> multiSignalDataRow(List<String> signalNames, long from, long to) {
        List<SignalDataRowList> multiSignalDataRows = new ArrayList<>();
        for (String signalName : signalNames)
        {
            multiSignalDataRows.add(
                    new SignalDataRowList(signalName, signalDataRepository.getAllSignalDataRow(signalName, from, to))
            );
        }
        return multiSignalDataRows;
    }

    public List<SignalDataRow> signalDataRow(String signalName, long from, long to) {
        return signalDataRepository.getAllSignalDataRow(signalName, from, to);
    }

    public List<SignalScoreRow> signalScoreData(String signalName, long from, long to) {
        return signalDataRepository.getSignalScoreData(signalName, from, to);
    }
}
