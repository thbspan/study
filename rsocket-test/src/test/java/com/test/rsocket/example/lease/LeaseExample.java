package com.test.rsocket.example.lease;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.lease.Lease;
import io.rsocket.lease.LeaseStats;
import io.rsocket.lease.Leases;
import io.rsocket.lease.MissingLeaseException;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.ByteBufPayload;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

public class LeaseExample {
    private static final String SERVER_TAG = "server";
    private static final String CLIENT_TAG = "client";

    public static void main(String[] args) {
        int queueCapacity = 50;
        BlockingQueue<String> messagesQueue = new ArrayBlockingQueue<>(queueCapacity);

        Thread workerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = messagesQueue.take();
                    System.out.printf("Process message %s\n", message);
                    TimeUnit.MILLISECONDS.sleep(500);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "worker");
        workerThread.start();
        CloseableChannel server = RSocketServer.create(((setup, sendingSocket) -> Mono.just(
                new RSocket() {
                    @Override
                    public Mono<Void> fireAndForget(Payload payload) {
                        try {
                            if (!messagesQueue.offer(payload.getDataUtf8())) {
                                System.out.println("Queue has been overflowed. Terminating execution");
                                sendingSocket.dispose();
                                workerThread.interrupt();
                            }
                        } finally {
                            payload.release();
                        }
                        return Mono.empty();
                    }
                })))
                .lease(() -> Leases.create().sender(new LeaseCalculator(SERVER_TAG, messagesQueue)))
                .bindNow(TcpServerTransport.create("localhost", 7000));

        LeaseReceiver receiver = new LeaseReceiver(CLIENT_TAG);

        RSocket clientRSocket = RSocketConnector.create()
                .lease(() -> Leases.create().receiver(receiver))
                .connect(TcpClientTransport.create(server.address()))
                .block();
        if (clientRSocket != null) {
            Flux.<Long, Long>generate(
                    () -> 0L,
                    ((l, synchronousSink) -> {
                        synchronousSink.next(l);
                        return l + 1L;
                    }))
                    .delaySubscription(receiver.notifyWhenNewLease().then())
                    .concatMap(tick -> {
                        System.out.printf("Requesting FireAndForget(%d)", tick);
                        return Mono.defer(() -> clientRSocket.fireAndForget(ByteBufPayload.create("" + tick)))
                                .retryWhen(Retry.indefinitely()
                                        .filter(t -> t instanceof MissingLeaseException)
                                        .doBeforeRetryAsync(rs -> {
                                            System.out.printf("Ran out of leases %s\n", rs);
                                            return receiver.notifyWhenNewLease().then();
                                        }));
                    })
                    .blockLast();
            clientRSocket.onClose().block();
        }
        server.dispose();
    }

    static class LeaseCalculator implements Function<Optional<LeaseStats>, Flux<Lease>> {
        final String tag;
        final BlockingQueue<?> queue;

        public LeaseCalculator(String tag, BlockingQueue<?> queue) {
            this.tag = tag;
            this.queue = queue;
        }

        @Override
        public Flux<Lease> apply(Optional<LeaseStats> leaseStats) {
            System.out.printf("%s stats are %s\n", leaseStats.isPresent() ? "present" : "absent", tag);
            Duration ttlDuration = Duration.ofSeconds(5);
            return Flux.interval(Duration.ZERO, ttlDuration.dividedBy(2))
                    .handle((__, sink) -> {
                        int requests = queue.remainingCapacity();
                        if (requests > 0) {
                            long ttl = ttlDuration.toMillis();
                            sink.next(Lease.create((int) ttl, requests));
                        }
                    });
        }
    }

    static class LeaseReceiver implements Consumer<Flux<Lease>> {
        final String tag;
        final Sinks.Many<Lease> lastLeaseReplay = Sinks.many().replay().latest();

        public LeaseReceiver(String tag) {
            this.tag = tag;
        }

        @Override
        public void accept(Flux<Lease> receivedLeases) {
            receivedLeases.subscribe(l -> {
                System.out.printf("%s received leases - ttl: %s, requests: %s\n", tag, l.getTimeToLiveMillis(), l.getAllowedRequests());
                lastLeaseReplay.tryEmitNext(l);
            });
        }

        public Mono<Lease> notifyWhenNewLease() {
            return lastLeaseReplay.asFlux().filter(Lease::isValid).next();
        }
    }
}
