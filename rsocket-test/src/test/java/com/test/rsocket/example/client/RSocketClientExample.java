package com.test.rsocket.example.client;

import java.time.Duration;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketClient;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class RSocketClientExample {
    public static void main(String[] args) {
        RSocketServer.create(SocketAcceptor.forRequestResponse(
                payload -> {
                    String data = payload.getDataUtf8();
                    System.out.println("[INFO] Received request data " + data);
                    Payload responsePayload = DefaultPayload.create("Echo: " + data);
                    payload.release();

                    return Mono.just(responsePayload);
                }))
                .bind(TcpServerTransport.create("localhost", 7000))
                .delaySubscription(Duration.ofSeconds(5))
                .doOnNext(cc -> System.out.println("Server started on the address : " + cc.address()))
                .block();

        Mono<RSocket> source = RSocketConnector.create()
                .reconnect(Retry.backoff(50, Duration.ofMillis(500)))
                .connect(TcpClientTransport.create("localhost", 7000));

        RSocketClient.from(source)
                .requestResponse(Mono.just(DefaultPayload.create("Test Request")))
                .doOnSubscribe(subscription -> System.out.println("Executing Request"))
                .doOnNext(payload -> System.out.println("[INFO] Received response data " + payload.getDataUtf8()))
                .repeat(10)
                .blockLast();
    }
}
