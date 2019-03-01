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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MutationTest {

    private static final String SPO2_NAME = "spo2";
    private static final double SPO2_FREQUENCY = 1546300800000L;
    private static final String SPO2_TIMESTAMP_STR = "2019.01.01 00:00:00.000";
    private static final List<Double> SPO2_DATAPOINTS = new ArrayList<>();
    private static final double[] SPO2_RAW_DATAPOINTS = new double[] {1.3, 2.5, 3.7};

    private static final long SPO2_TIMESTAMP_FROM = 1546300800000L;
    private static final long SPO2_TIMESTAMP_TO = 1546300801000L;
    private static final double SPO2_VALUE = 1.3;

    static {
        for (double dataPoint : SPO2_RAW_DATAPOINTS) {
            SPO2_DATAPOINTS.add(dataPoint);
        }
    }

    private static final long AGGREGATE_TIMESTAMP = 1546300802000L;
    private static final double AGGREGATE_VALUE = 7.5;
    private static final double AGGREGATE_SPO2 = 1.5;
    private static final double AGGREGATE_ECG = 3.0;
    private static final double AGGREGATE_BP = 0.0;
    private static final double AGGREGATE_RESP = 2.0;
    private static final double AGGREGATE_TEMP = 1.0;


    private Mutation mutation;

    @Mock
    private SignalDataRepository signalDataRepository;

    @BeforeEach
    void setUp() {
        mutation = new Mutation(signalDataRepository);
    }

    @AfterEach
    void tearDown() {
        mutation = null;
    }

    @Test
    void storeSignalData() throws Exception {
        SignalData expectedSignalData = new SignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STR, SPO2_DATAPOINTS);
        SignalData actualSignalData = mutation.storeSignalData(SPO2_NAME, SPO2_FREQUENCY, SPO2_TIMESTAMP_STR, SPO2_DATAPOINTS);
        assertEquals(expectedSignalData, actualSignalData);
        verify(signalDataRepository).saveSignalData(SPO2_NAME, actualSignalData);
    }

    @Test
    void storeSignalScore() throws Exception {
        SignalScoreRow expectedSignalScore = new SignalScoreRow(SPO2_TIMESTAMP_FROM, SPO2_TIMESTAMP_TO, SPO2_VALUE);
        SignalScoreRow actualSignalScore = mutation.storeSignalScore(SPO2_NAME, SPO2_TIMESTAMP_FROM, SPO2_TIMESTAMP_TO, SPO2_VALUE);
        assertEquals(expectedSignalScore, actualSignalScore);
        verify(signalDataRepository).saveSignalScore(SPO2_NAME, actualSignalScore);
    }

    @Test
    void storeAggregatedScore() throws Exception {
        AggregatedScoreRow expectedAggregatedScoreRow = new AggregatedScoreRow(AGGREGATE_TIMESTAMP, AGGREGATE_VALUE, AGGREGATE_SPO2,
                AGGREGATE_ECG, AGGREGATE_BP, AGGREGATE_RESP, AGGREGATE_TEMP);
        AggregatedScoreRow actualAggregatedScoreRow = mutation.storeAggregatedScore(AGGREGATE_TIMESTAMP, AGGREGATE_VALUE, AGGREGATE_SPO2,
                AGGREGATE_ECG, AGGREGATE_BP, AGGREGATE_RESP, AGGREGATE_TEMP);
        assertEquals(expectedAggregatedScoreRow, actualAggregatedScoreRow);
        verify(signalDataRepository).saveAggregatedScore(actualAggregatedScoreRow);
    }
}