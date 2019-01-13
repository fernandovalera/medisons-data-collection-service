package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueryTest {

    private static final String SPO2_NAME = "spo2";
    private static final long SPO2_TIMESTAMP_1 = 1546300800000L;
    private static final long SPO2_TIMESTAMP_2 = 1546300801000L;

    @InjectMocks
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
    void allSignalData() {
        query.allSignalData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
    }

    @Test
    void signalDataRow() {
        query.signalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        verify(signalDataRepository).getAllSignalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
    }

    @Test
    void signalScoreData() {
        query.signalScoreData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
        verify(signalDataRepository).getSignalScoreData(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);
    }
}