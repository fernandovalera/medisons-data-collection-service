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
            multiSignalDataList.addAll(this.allSignalData(signalName, from, to));
        }
        return multiSignalDataList;
    }

    public List<SignalData> allSignalData(String baseSignalName, long from, long to) throws Exception {
        List<String> signalNames = signalDataRepository.getSignalTableNamesFromBaseName(baseSignalName);
        List<SignalData> signalDataList = new ArrayList<>();
        for (String signalName : signalNames) {
            signalDataList.add(signalDataRepository.getAllSignalData(signalName, from, to));
        }
        return signalDataList;
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

    public List<SignalScoreRowListItem> lastSignalScoreRowsInRange(long from, long to) throws Exception {
        return signalDataRepository.getLastSignalScoreRowsInRange(from, to);
    }

    public List<AggregatedScoreRow> aggregatedScoreRows(long from, long to) throws Exception {
        return signalDataRepository.getAllAggregatedScoreRow(from, to);
    }
}
