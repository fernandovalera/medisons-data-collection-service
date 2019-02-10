package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueryTest {

    private static final String SPO2_NAME = "spo2";
    private static final String BP_NAME = "bp";

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
        query.allSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
    }
  
    @Test
    void multiSignalData() throws Exception {
        List<String> expectedNames = new ArrayList<>();
        expectedNames.add(SPO2_NAME);
        expectedNames.add(BP_NAME);

        query.multiSignalData(expectedNames, TIMESTAMP_1, TIMESTAMP_2);

        verify(signalDataRepository).getAllSignalData(SPO2_NAME, TIMESTAMP_1, TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(BP_NAME, TIMESTAMP_1, TIMESTAMP_2);
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