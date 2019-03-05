package com.medisons.dbm;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import java.util.ArrayList;
import java.util.List;

public class Query implements GraphQLQueryResolver {

    private final SignalDataRepository signalDataRepository;

    public Query(SignalDataRepository signalDataRepository) {
        this.signalDataRepository = signalDataRepository;
    }

    public List<SignalDataRowList> multiSignalDataRows(List<String> signalNames, long from, long to) throws Exception {
        List<SignalDataRowList> multiSignalDataList = new ArrayList<>();
        for (String signalName : signalNames)
        {
            multiSignalDataList.addAll(this.signalDataRows(signalName, from, to));
        }
        return multiSignalDataList;
    }

    public List<SignalDataRowList> signalDataRows(String baseSignalName, long from, long to) throws Exception {
        List<String> signalNames = signalDataRepository.getSignalTableNamesFromBaseName(baseSignalName);
        List<SignalDataRowList> signalDataList = new ArrayList<>();
        for (String signalName : signalNames) {
            signalDataList.add(signalDataRepository.getSignalDataRowList(signalName, from, to));
        }
        return signalDataList;
    }

    public List<SignalScoreRow> signalScoreData(String signalName, long from, long to) throws Exception {
        return signalDataRepository.getSignalScoreData(signalName, from, to);
    }

    public List<SignalScoreRowListItem> lastSignalScoreRowsInRange(long from, long to) throws Exception {
        return signalDataRepository.getLastSignalScoreRowsInRange(from, to);
    }

    public AggregatedScoreRowList aggregatedScoreRows(long from, long to) throws Exception {
        return signalDataRepository.getAggregatedScoreRowList(from, to);
    }
}
