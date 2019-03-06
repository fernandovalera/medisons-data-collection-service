package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QueryTest {

    private static final String SPO2_NAME = "spo2";
    private static final String BP_NAME = "bp";
    private static final String BP_SYS_NAME = "bp_sys";
    private static final String BP_DIA_NAME = "bp_dia";

    private static final long TIMESTAMP_1 = 1546300800000L;
    private static final long TIMESTAMP_2 = 1546300801000L;

    private Query query;

    @Mock
    SignalDataRepository signalDataRepository;

    @BeforeEach
    void setUp() {
        query = new Query(signalDataRepository);
    }

    @AfterEach
    void tearDown() {
        query = null;
    }

    @Test
    void allSignalData_givenSingleVital_retrieveOneSignalData() throws Exception {
        List<String> spo2SignalTableNames = new ArrayList<>();
        spo2SignalTableNames.add(SPO2_NAME);
        when(signalDataRepository.getSignalTableNamesFromBaseName(SPO2_NAME)).thenReturn(spo2SignalTableNames);
        when(signalDataRepository.getSignalDataRowList(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2))
                .thenReturn(mock(SignalDataRowList.class));

        List<SignalDataRowList> result = query.signalDataRows(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getSignalDataRowList(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        assertEquals(1, result.size());
    }

    @Test
    void allSignalData_givenDoubleVital_retrieveTwoSignalData() throws Exception {
        List<String> bpSignalTableNames = new ArrayList<>();
        bpSignalTableNames.add(BP_DIA_NAME);
        bpSignalTableNames.add(BP_SYS_NAME);
        when(signalDataRepository.getSignalTableNamesFromBaseName(BP_NAME)).thenReturn(bpSignalTableNames);
        when(signalDataRepository.getSignalDataRowList(anyString(), eq(TIMESTAMP_1), eq(TIMESTAMP_2)))
                .thenReturn(mock(SignalDataRowList.class));

        List<SignalDataRowList> result = query.signalDataRows(BP_NAME, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getSignalDataRowList(BP_DIA_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getSignalDataRowList(BP_SYS_NAME, TIMESTAMP_1, TIMESTAMP_2);
        assertEquals(2, result.size());
    }

    @Test
    void multiSignalData_givenSingleAndDoubleVital_retrieveThreeSignalData() throws Exception {
        List<String> spo2SignalTableNames = new ArrayList<>();
        spo2SignalTableNames.add(SPO2_NAME);
        List<String> bpSignalTableNames = new ArrayList<>();
        bpSignalTableNames.add(BP_DIA_NAME);
        bpSignalTableNames.add(BP_SYS_NAME);
        when(signalDataRepository.getSignalTableNamesFromBaseName(anyString()))
                .thenReturn(spo2SignalTableNames)
                .thenReturn(bpSignalTableNames);
        when(signalDataRepository.getSignalDataRowList(anyString(), eq(TIMESTAMP_1), eq(TIMESTAMP_2)))
                .thenReturn(mock(SignalDataRowList.class));

        List<String> expectedNames = new ArrayList<>();
        expectedNames.add(SPO2_NAME);
        expectedNames.add(BP_NAME);
        List<SignalDataRowList> result = query.multiSignalDataRows(expectedNames, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getSignalDataRowList(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getSignalDataRowList(BP_DIA_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getSignalDataRowList(BP_SYS_NAME, TIMESTAMP_1, TIMESTAMP_2);
        assertEquals(3, result.size());
    }

    @Test
    void signalScoreData() throws Exception {
        query.signalScoreData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getSignalScoreData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test
    void lastSignalScoreRowsInRange() throws Exception {
        query.lastSignalScoreRowsInRange(TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getLastSignalScoreRowsInRange(TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test
    void aggregatedScoreRows() throws Exception {
        query.aggregatedScoreRows(TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAggregatedScoreRowList(TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test
    void backgroundData() throws Exception {
        query.backgroundData();
        verify(signalDataRepository).getBackgroundData();
    }
}