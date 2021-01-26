package com.test.rsocket.example.resume;

import java.time.Duration;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

public class ResumeFileTransfer {
    private static final int PREFETCH_WINDOW_SIZE = 4;

    public static void main(String[] args) {
        Resume resume = new Resume()
                .sessionDuration(Duration.ofMinutes(5))
                .retry(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(1))
                        .doBeforeRetry(s -> System.out.println("Disconnected. Trying to resume...")));

        CloseableChannel server = RSocketServer.create(SocketAcceptor.forRequestStream(
                payload -> {
                    Request request = RequestCodec.decode(payload);
                    payload.release();
                    String fileName = request.getFileName();
                    int chunkSize = request.getChunkSize();
                    Flux<Long> ticks = Flux.interval(Duration.ofMillis(500)).onBackpressureDrop();

                    return Files.fileSource(fileName, chunkSize)
                            .map(DefaultPayload::create)
                            .zipWith(ticks, (p, tick) -> p)
                            .log("server");
                }))
                .resume(resume)
                .bindNow(TcpServerTransport.create("localhost", 8000));

        RSocket client = RSocketConnector.create()
                .resume(resume)
                .connect(TcpClientTransport.create("localhost", 8001))
                .block();
        if (client != null) {
            client.requestStream(RequestCodec.encode(new Request(16, "lorem.txt")))
                    .log("client")
                    .doFinally(s -> server.dispose())
                    .subscribe(Files.fileSink("rsocket-examples/build/lorem_output.txt", PREFETCH_WINDOW_SIZE));
        }

        server.onClose().block();
    }

    static class RequestCodec {
        public static Payload encode(Request request) {
            return DefaultPayload.create(request.getChunkSize() + ":" + request.getFileName());
        }

        public static Request decode(Payload payload) {
            String encoded = payload.getDataUtf8();
            String[] chunkSizeAndFileName = encoded.split(":");
            return new Request(Integer.parseInt(chunkSizeAndFileName[0]), chunkSizeAndFileName[1]);
        }
    }

    static class Request {
        private final int chunkSize;
        private final String fileName;

        public Request(int chunkSize, String fileName) {
            this.chunkSize = chunkSize;
            this.fileName = fileName;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
