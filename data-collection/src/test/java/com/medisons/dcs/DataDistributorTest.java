package com.medisons.dcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class DataDistributorTest {

    private final static String SIGNAL_DATA_NAME_1 = "Aa";
    private final static double SIGNAL_DATA_FREQUENCY_1 = 0.9;
    private final static String SIGNAL_DATA_TIMESTAMP_1 = "2018.01.01 00:00:00.000";
    private final static List<Double> SIGNAL_DATA_DATA_POINTS_1 = Arrays.asList(1.05, 1.10, 1.15);

    @Mock
    SignalData signalDataMock;

    @Mock
    HttpClient httpClientMock;

    @Mock
    HttpResponse<String> httpResponseMock;

    private DataDistributor dataDistributor = null;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(signalDataMock.getSignalName()).thenReturn(SIGNAL_DATA_NAME_1);
        Mockito.when(signalDataMock.getSignalFrequency()).thenReturn(SIGNAL_DATA_FREQUENCY_1);
        Mockito.when(signalDataMock.getSignalTimestamp()).thenReturn(SIGNAL_DATA_TIMESTAMP_1);
        Mockito.when(signalDataMock.getDataPoints()).thenReturn(SIGNAL_DATA_DATA_POINTS_1);

        dataDistributor = new DataDistributor(httpClientMock);
    }

    @AfterEach
    void tearDown() {
        dataDistributor = null;
    }

    @Test
    void storeDataPoints_givenValidSignalData_returnZero() throws IOException, InterruptedException {
        Mockito.when(httpClientMock.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(httpResponseMock);
        Mockito.when(httpResponseMock.statusCode()).thenReturn(200);
        assertEquals(0, dataDistributor.storeSignalData(signalDataMock));
    }

    @Test
    void storeDataPoints_givenException_returnError() {
        try {
            Mockito.when(httpClientMock.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);
        } catch (IOException | InterruptedException e) {
            fail();
        }

        assertEquals(-1, dataDistributor.storeSignalData(signalDataMock));

        try {
            Mockito.when(httpClientMock.send(any(), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(InterruptedException.class);
        } catch (IOException | InterruptedException e) {
            fail();
        }

        assertEquals(-1, dataDistributor.storeSignalData(signalDataMock));
    }
}