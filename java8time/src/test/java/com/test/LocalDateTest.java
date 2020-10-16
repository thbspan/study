package com.test;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.Test;

public class LocalDateTest {

    @Test
    public void test() {
        LocalDate localDate = LocalDate.now();
        System.out.println(localDate.with(TemporalAdjusters.lastDayOfYear()));
    }
}
