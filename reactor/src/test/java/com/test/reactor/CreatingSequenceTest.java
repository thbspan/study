package com.test.reactor;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

public class CreatingSequenceTest {

    @Test
    public void testGenerate() {
        Flux<String> generate = Flux.generate(() -> 0, (integer, synchronousSink) -> {
            synchronousSink.next(" 3 x " + integer + " = " + 3 * integer);
            if (integer == 10) {
                synchronousSink.complete();
            }
            return integer + 1;
        });
        generate.subscribe(System.out::println);
    }

    @Test
    public void testMutable() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3 * i);
                    if (i == 9) sink.complete();
                    return state;
                });
        flux.subscribe(System.out::println);
    }

    @Test
    public void testCreate() {
        // 支持多线程方式
        Flux.create(sink -> {
            sink.next("create0");
            sink.next("create1");
            sink.complete();
        }).subscribe(System.out::println);
    }
}
