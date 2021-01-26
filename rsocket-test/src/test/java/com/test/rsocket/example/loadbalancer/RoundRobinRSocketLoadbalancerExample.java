package com.test.rsocket.example.loadbalancer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketClient;
import io.rsocket.core.RSocketServer;
import io.rsocket.loadbalance.LoadbalanceRSocketClient;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RoundRobinRSocketLoadbalancerExample {
    public static void main(String[] args) {
        CloseableChannel server1 = RSocketServer.create(SocketAcceptor.forRequestResponse(payload -> {
            System.out.println("Server 1 got fnf " + payload.getDataUtf8());
            return Mono.just(DefaultPayload.create("Server 1 response"))
                    .delayElement(Duration.ofMillis(100));
        })).bindNow(TcpServerTransport.create(8080));

        CloseableChannel server2 = RSocketServer.create(SocketAcceptor.forRequestResponse(payload -> {
            System.out.println("Server 2 got fnf " + payload.getDataUtf8());
            return Mono.just(DefaultPayload.create("Server 2 response"))
                    .delayElement(Duration.ofMillis(100));
        })).bindNow(TcpServerTransport.create(8081));

        CloseableChannel server3 = RSocketServer.create(SocketAcceptor.forRequestResponse(payload -> {
            System.out.println("Server 3 got fnf " + payload.getDataUtf8());
            return Mono.just(DefaultPayload.create("Server 3 response"))
                    .delayElement(Duration.ofMillis(100));
        })).bindNow(TcpServerTransport.create(8082));

        LoadbalanceTarget target8080 = LoadbalanceTarget.from("8080", TcpClientTransport.create(8080));
        LoadbalanceTarget target8081 = LoadbalanceTarget.from("8081", TcpClientTransport.create(8081));
        LoadbalanceTarget target8082 = LoadbalanceTarget.from("8082", TcpClientTransport.create(8082));

        Flux<List<LoadbalanceTarget>> producer = Flux.interval(Duration.ofSeconds(5))
                .doOnNext(System.out::println)
                .map(i -> {
                    int val = i.intValue();
                    switch (val) {
                        case 0:
                        case 6:
                        case 7:
                            return Collections.emptyList();
                        case 1:
                            return Collections.singletonList(target8080);
                        case 2:
                            return Arrays.asList(target8080, target8081);
                        case 3:
                            return Arrays.asList(target8080, target8082);
                        case 4:
                            return Arrays.asList(target8081, target8082);
                        default:
                            return Arrays.asList(target8080, target8081, target8082);
                    }
                });
        RSocketClient rSocketClient = LoadbalanceRSocketClient.builder(producer).roundRobinLoadbalanceStrategy().build();
        for (int i = 0; i < 1000; i++) {
            try {
                rSocketClient.requestResponse(Mono.just(DefaultPayload.create("test" + i))).block();
            } catch (Throwable t) {
                // no ops
            }
        }
    }
}
