package com.test.rsocket.example.channel;

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

public class ChannelEchoClient {

    public static void main(String[] args) {
        SocketAcceptor echoAcceptor = SocketAcceptor.forRequestChannel(
                payloads -> Flux.from(payloads)
                        .map(Payload::getDataUtf8)
                        .map(s -> "Echo:" + s)
                        .map(DefaultPayload::create));
        RSocketServer.create(echoAcceptor).bindNow(TcpServerTransport.create("localhost", 7000));

        RSocket socket =
                RSocketConnector.connectWith(TcpClientTransport.create("localhost", 7000)).block();

        if (socket != null) {
            socket.requestChannel(
                    Flux.interval(Duration.ofMillis(1000)).map(i -> DefaultPayload.create("Hello")))
                    .map(Payload::getDataUtf8)
                    .doOnNext(System.out::println)
                    .take(10)
                    .doFinally(signalType -> socket.dispose())
                    .then()
                    .block();
        }
    }
}
