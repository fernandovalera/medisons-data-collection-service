package com.medisons.dcs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SignalDataReaderTest {

    private InputStream mockInputStream;

    @BeforeEach
    void setUp() {
        mockInputStream = new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        };
    }

    @AfterEach
    void tearDown() {
        mockInputStream = null;
    }

    @Test
    void ctor_givenMockInputStream_success() {
        new SignalDataReader(mockInputStream);
    }

    @Test
    void getDataPacket_givenEndOfDataPacketNegative_throwException() {
        SignalDataReader dataReader = new SignalDataReader(mockInputStream) {
            protected int readToEndOfDataPacket() {
                return -1;
            }
        };

        assertThrows(IOException.class, dataReader::getDataPacket);
    }

    @Test
    void getDataPacket_givenValidDataPacket_returnSignalDataPacket() {
        SignalData mockSignalData = new SignalData(
                "a",1d,"2018.01.01 00:00:00.000", new ArrayList<>());

        SignalDataReader dataReader = new SignalDataReader(mockInputStream) {
            protected int readToEndOfDataPacket() {
                return 1;
            }

            protected SignalData parseSignalDataPacket(int endOfPacket) {
                return mockSignalData;
            }

            protected void resetDataBuffer(int endOfPacket) { }
        };

        try {
            assertEquals(mockSignalData, dataReader.getDataPacket());
        } catch (IOException e) {
            fail();
        }
    }
}