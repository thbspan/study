package com.test.reactor;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class HotAndColdProcessorTest {

    /**
     * 冷序列
     */
    @Test
    public void testColdSequence() {
        Flux<String> source = Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                .map(String::toUpperCase);

        source.subscribe(d -> System.out.println("Subscriber 1: " + d));
        System.out.println();
        source.subscribe(d -> System.out.println("Subscriber 2: " + d));
    }

    /**
     * 热序列
     */
    @Test
    public void testHotSequence() {
        // multicast 允许有多个subscribe
        // unicast 只允许有一个subscribe
        Sinks.Many<String> objectMany = Sinks.many().multicast().onBackpressureBuffer();
        Flux<String> hotFlux = objectMany.asFlux()
                .map(String::toUpperCase);

        hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: " + d));
        // 手动发送元素
        objectMany.tryEmitNext("blue");
        objectMany.tryEmitNext("green");
        hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: " + d));
        objectMany.tryEmitNext("orange");
        objectMany.tryEmitNext("purple");
        objectMany.tryEmitComplete();
    }
}
