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

public class ClientStreamingToServer {

    public static void main(String[] args) {
        RSocketServer.create(SocketAcceptor.forRequestStream(payload -> Flux.interval(Duration.ofMillis(100))
                .map(aLong -> DefaultPayload.create("Interval: " + aLong))))
                .bindNow(TcpServerTransport.create("localhost", 7000));
        RSocket socket = RSocketConnector.create()
                .setupPayload(DefaultPayload.create("test", "test"))
                .connect(TcpClientTransport.create("localhost", 7000))
                .block();

        if (socket != null) {
            Payload payload = DefaultPayload.create("Hello");
            socket.requestStream(payload)
                    .map(Payload::getDataUtf8)
                    .doOnNext(System.out::println)
                    .take(10L)
                    .then()
                    .doFinally(__ -> socket.dispose())
                    .then()
                    .block();
        }
    }
}
