package com.test.rsocket.example.requestresponse;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Mono;

public class HelloWorldClient {
    public static void main(String[] args) {
        RSocket rSocket = new RSocket() {
            boolean fail = true;

            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                if (fail) {
                    fail = false;
                    return Mono.error(new Throwable("Simulated error"));
                } else {
                    return Mono.just(payload);
                }
            }
        };
        RSocketServer.create(SocketAcceptor.with(rSocket))
                .bindNow(TcpServerTransport.create("localhost", 7000));
        RSocket socket =
                RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7000)).block();

        if (socket != null) {
            for (int i = 0; i < 3; i++) {
                socket.requestResponse(DefaultPayload.create("Hello"))
                        .map(Payload::getDataUtf8)
                        .onErrorReturn("error")
                        .doOnNext(System.out::println)
                        .block();
            }
            socket.dispose();
        }
    }
}
