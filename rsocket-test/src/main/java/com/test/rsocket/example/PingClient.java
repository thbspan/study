package com.test.rsocket.example;

import java.time.Duration;
import java.util.function.BiFunction;

import org.HdrHistogram.Recorder;
import org.reactivestreams.Publisher;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.ByteBufPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PingClient {
    private final Payload payload;
    private final Mono<RSocket> client;

    public PingClient(Mono<RSocket> client) {
        this.client = client;
        this.payload = ByteBufPayload.create("hello");
    }

    public Recorder startTracker(Duration interval) {
        final Recorder histogram = new Recorder(3600000000000L, 3);

        Flux.interval(interval)
                .doOnNext(aLong -> {
                    System.out.println("---- PING/ PONG HISTO ----");
                    histogram.getIntervalHistogram()
                            .outputPercentileDistribution(System.out, 5, 1000.0, false);
                    System.out.println("---- PING/ PONG HISTO ----");
                }).subscribe();
        return histogram;
    }

    public Flux<Payload> requestResponsePingPong(int count, final Recorder histogram) {
        return pingPong(RSocket::requestResponse, count, histogram);
    }

    public Flux<Payload> requestStreamPingPong(int count, final Recorder histogram) {
        return pingPong(RSocket::requestStream, count, histogram);
    }

    Flux<Payload> pingPong(BiFunction<RSocket, ? super Payload, ? extends Publisher<Payload>> interaction, int count, final Recorder histogram) {
        return client
                .flatMapMany(rSocket ->
                        Flux.range(1, count).flatMap(i -> {
                            long start = System.nanoTime();
                            return Flux.from(interaction.apply(rSocket, payload.retain()))
                                    .doOnNext(Payload::release)
                                    .doFinally(signalType -> {
                                        long diff = System.nanoTime() - start;
                                        histogram.recordValue(diff);
                                    });
                        }, 64))
                .doOnError(Throwable::printStackTrace);
    }
}
