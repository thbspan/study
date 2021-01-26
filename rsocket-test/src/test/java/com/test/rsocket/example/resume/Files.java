package com.test.rsocket.example.resume;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.SynchronousSink;

class Files {

    public static Flux<ByteBuf> fileSource(String fileName, int chunkSize) {
        return Flux.generate(() -> new FileState(fileName, chunkSize), FileState::consumeNext, FileState::dispose);
    }

    public static Subscriber<Payload> fileSink(String fileName, int windowSize) {
        return new BaseSubscriber<Payload>() {
            Subscription subscription;
            int requests = windowSize;
            int receivedBytes;
            int receivedCount;
            OutputStream outputStream;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(requests);
            }

            @Override
            protected void hookOnNext(Payload payload) {
                ByteBuf data = payload.data();
                receivedBytes += data.readableBytes();
                receivedCount++;
                System.out.printf("Received file chunk: %d. Total size:%d\n", receivedCount, receivedBytes);
                if (outputStream == null) {
                    outputStream = open(fileName);
                }
                write(outputStream, data);
                payload.release();

                requests--;
                if (requests == windowSize / 2) {
                    requests += windowSize;
                    subscription.request(windowSize);
                }
            }

            @Override
            protected void hookFinally(SignalType type) {
                close(outputStream);
            }

            private void close(OutputStream outputStream) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            private void write(OutputStream outputStream, ByteBuf byteBuf) {
                try {
                    byteBuf.readBytes(outputStream, byteBuf.readableBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            private OutputStream open(String fileName) {
                try {
                    /*do not buffer for demo purposes*/
                    return new FileOutputStream(fileName);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    static class FileState {
        private final String fileName;
        private final int chunkSizeBytes;
        private BufferedInputStream inputStream;
        private byte[] chunkBytes;

        public FileState(String fileName, int chunkSizeBytes) {
            this.fileName = fileName;
            this.chunkSizeBytes = chunkSizeBytes;
        }

        public FileState consumeNext(SynchronousSink<ByteBuf> sink) {
            if (inputStream == null) {
                InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
                if (in == null) {
                    sink.error(new FileNotFoundException(fileName));
                    return this;
                } else {
                    this.inputStream = new BufferedInputStream(in);
                    this.chunkBytes = new byte[chunkSizeBytes];
                }
            }
            try {
                int consumedBytes = inputStream.read(chunkBytes);
                if (consumedBytes == -1) {
                    sink.complete();
                } else {
                    sink.next(Unpooled.copiedBuffer(chunkBytes, 0, consumedBytes));
                }
            } catch (IOException e) {
                sink.error(e);
            }
            return this;
        }

        public void dispose() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
