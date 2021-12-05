package com.test;

import java.time.Duration;

import org.junit.jupiter.api.Test;

public class DurationTest {

    @Test
    public void test() {
        System.out.println(Duration.parse("PT20.345S"));
        System.out.println(Duration.parse("PT1h"));
        System.out.println(Duration.parse("P30d"));
    }
}
