package com.test;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class ClockTest {

    @Test
    public void testToInstant() throws InterruptedException {
        //获得一个原始钟表，以格林威治标准时间为准
        Clock clock = Clock.systemDefaultZone();
        System.out.println(clock);
        //获得一个嘀嗒间隔5秒的tickClock钟表
        Clock clock1 = Clock.tick(clock, Duration.ofSeconds(5));
        System.out.println(clock1);

        for (int i = 0; i < 15; i++) {
            //每隔1秒取样一次
            TimeUnit.MILLISECONDS.sleep(1000);
            System.out.println("---");
            //原始钟表打印时间戳
            System.out.println(clock.instant());
            //tickClock钟表打印时间戳
            System.out.println(clock1.instant() + " tick钟表");
        }
    }

    @Test
    public void testTickSeconds() throws InterruptedException {
        Clock c = Clock.tickSeconds(ZoneId.of("GMT"));
        for (int i = 0; i < 10; i++) {
            //每半秒读一次，一秒内读两次
            Thread.sleep(500);
            System.out.println(c.instant());
        }
    }
}
