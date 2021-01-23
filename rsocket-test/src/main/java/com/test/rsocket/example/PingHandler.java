package com.test.rsocket.example;

import java.util.concurrent.ThreadLocalRandom;

import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.util.ByteBufPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class PingHandler implements SocketAcceptor {
    private final Payload pong;

    public PingHandler() {
        byte[] data = new byte[1024];
        ThreadLocalRandom.current().nextBytes(data);
        pong = ByteBufPayload.create(data);
    }

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
        return Mono.just(new RSocket() {
            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                payload.release();
                return Mono.just(pong.retain());
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                payload.release();
                return Flux.range(0, 100).map(v -> pong.retain());
            }
        });
    }
}
