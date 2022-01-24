package com.kyu.calculator;

import jdk.jshell.spi.ExecutionControl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.RuntimeUtils;

import static org.junit.jupiter.api.Assertions.*;

class StringCalculatorTest {
    private StringCalculator stringCalculator;

    @BeforeEach
    public void setUp() {
        stringCalculator = new StringCalculator();
    }

    @Test
    public void add_널공백체크() {
        assertEquals(0, stringCalculator.add(""));
        assertEquals(0, stringCalculator.add(null));
    }

    @Test
    public void add_단일숫자() {
        assertEquals(1, stringCalculator.add("1"));
    }

    @Test
    public void add_쉼표더하기() {
        assertEquals(3, stringCalculator.add("1,2"));
    }

    @Test
    public void add_쉼표콜론더하기() {
        assertEquals(6, stringCalculator.add("1,2:3"));
    }

    @Test
    public void add_커스텀구분자() {
        assertEquals(6, stringCalculator.add("//;\n1;2;3"));
    }

    @Test
    public void add_음수오류처리() {
        Exception exception = assertThrows(RuntimeException.class , () -> {
            stringCalculator.add("-1,2,3");
        });
    }
}