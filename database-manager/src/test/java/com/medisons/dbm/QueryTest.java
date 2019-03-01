package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryTest {

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
    void allSignalData() throws Exception {
        List<String> signalTableNames = new ArrayList<>();
        signalTableNames.add(SPO2_NAME);
        when(signalDataRepository.getSignalTableNamesFromBaseName(SPO2_NAME)).thenReturn(signalTableNames);
        query.allSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
    }
  
    @Test
    void multiSignalData() throws Exception {
        List<String> spo2SignalTableNames = new ArrayList<>();
        spo2SignalTableNames.add(SPO2_NAME);
        List<String> bpSignalTableNames = new ArrayList<>();
        bpSignalTableNames.add(BP_DIA_NAME);
        bpSignalTableNames.add(BP_SYS_NAME);
        when(signalDataRepository.getSignalTableNamesFromBaseName(anyString()))
                .thenReturn(spo2SignalTableNames)
                .thenReturn(bpSignalTableNames);

        List<String> expectedNames = new ArrayList<>();
        expectedNames.add(SPO2_NAME);
        expectedNames.add(BP_NAME);

        query.multiSignalData(expectedNames, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getAllSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(BP_DIA_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(BP_SYS_NAME, TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test
    void multiSignalDataRow() throws Exception {
        List<String> expectedNames = new ArrayList<>();
        expectedNames.add(SPO2_NAME);
        expectedNames.add(BP_NAME);

        query.multiSignalDataRow(expectedNames, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getAllSignalDataRow(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalDataRow(BP_NAME, TIMESTAMP_1, TIMESTAMP_2);
    }

    @Test
    void signalDataRow() throws Exception {
        query.signalDataRow(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalDataRow(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
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
}