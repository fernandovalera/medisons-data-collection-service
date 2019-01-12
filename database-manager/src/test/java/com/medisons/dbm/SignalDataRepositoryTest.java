package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalDataRepositoryTest {

    private static final String SPO2_NAME = "spo2";

    private static final String GET_SPO2_DATA_QUERY = "SELECT timestampMilli, value FROM spO2 WHERE timestampMilli BETWEEN ? AND ? ORDER BY timestampMilli";

    private static final long SPO2_TIMESTAMP_1 = 100L;
    private static final long SPO2_TIMESTAMP_2 = 101L;

    private static final double SPO2_VALUE_1 = 1.5D;
    private static final double SPO2_VALUE_2 = 2.7D;

    @InjectMocks
    SignalDataRepository signalDataRepository;

    @Mock
    Connection connection;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAllSignalData() {
    }

    @Test
    void getAllSignalDataRow_givenSPO2Data_returnTwoItems() throws SQLException{
        when(connection.prepareStatement(eq(GET_SPO2_DATA_QUERY))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getLong(1)).thenReturn(SPO2_TIMESTAMP_1).thenReturn(SPO2_TIMESTAMP_2);
        when(resultSet.getDouble(2)).thenReturn(SPO2_VALUE_1).thenReturn(SPO2_VALUE_2);

        List<SignalDataRow> result = signalDataRepository.getAllSignalDataRow(SPO2_NAME, SPO2_TIMESTAMP_1, SPO2_TIMESTAMP_2);

        assertEquals(2, result.size());

        assertEquals(SPO2_TIMESTAMP_1, result.get(1).getTimestampMilli());
        assertEquals(SPO2_VALUE_1, result.get(1).getValue());

        assertEquals(SPO2_TIMESTAMP_2, result.get(2).getTimestampMilli());
        assertEquals(SPO2_VALUE_2, result.get(2).getValue());
    }

    @Test
    void saveSignalData() {
    }
}