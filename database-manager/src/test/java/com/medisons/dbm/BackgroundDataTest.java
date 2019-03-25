package com.medisons.dbm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BackgroundDataTest {

    private static final Integer AGE = 25;
    private static final Integer WEIGHT = 65;
    private static final Integer HEIGHT = 170;
    private static final String SEX = "F";

    private BackgroundData backgroundData;

    @BeforeEach
    void setUp() {
        backgroundData = new BackgroundData(AGE, WEIGHT, HEIGHT, SEX);
    }

    @AfterEach
    void tearDown() {
        backgroundData = null;
    }

    @Test
    void getAge() {
        assertEquals(AGE, backgroundData.getAge());
    }

    @Test
    void getWeight() {
        assertEquals(WEIGHT, backgroundData.getWeight());
    }

    @Test
    void getHeight() {
        assertEquals(HEIGHT, backgroundData.getHeight());
    }

    @Test
    void getSex() {
        assertEquals(SEX, backgroundData.getSex());
    }

    @Test
    void fields_areNullable() {
        backgroundData = new BackgroundData(null, null, null, null);
        assertNull(backgroundData.getAge());
        assertNull(backgroundData.getWeight());
        assertNull(backgroundData.getHeight());
        assertNull(backgroundData.getSex());
    }
}
