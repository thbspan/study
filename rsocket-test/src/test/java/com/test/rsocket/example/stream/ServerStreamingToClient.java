package com.test.rsocket.example.stream;

import java.time.Duration;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ServerStreamingToClient {

    public static void main(String[] args) {
        RSocketServer.create((setup, rSocket) -> {
            rSocket.requestStream(DefaultPayload.create("Hello-bidi"))
                    .map(Payload::getDataUtf8)
                    .log()
                    .subscribe();
            return Mono.just(new RSocket() {});
        }).bindNow(TcpServerTransport.create("localhost", 7000));

        RSocket rSocket = RSocketConnector.create()
                .acceptor(SocketAcceptor.forRequestStream(payload ->
                        Flux.interval(Duration.ofSeconds(1))
                                .map(aLong -> DefaultPayload.create("Bi-di Response => " + aLong))))
                .connect(TcpClientTransport.create("localhost", 7000))
                .block();
        if (rSocket != null) {
            rSocket.onClose().block();
        }
    }
}
