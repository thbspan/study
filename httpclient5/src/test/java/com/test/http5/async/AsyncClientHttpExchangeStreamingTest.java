package com.test.http5.async;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.AbstractCharResponseConsumer;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

public class AsyncClientHttpExchangeStreamingTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

        client.start();

        final HttpHost target = new HttpHost("httpbin.org");
        final String[] requestUris = new String[]{"/", "/ip", "/user-agent", "/headers"};

        for (final String requestUri : requestUris) {
            final BasicHttpRequest request = BasicRequestBuilder.get()
                    .setHttpHost(target)
                    .setPath(requestUri)
                    .build();

            System.out.println("Executing request " + request);
            final Future<Void> future = client.execute(
                    new BasicRequestProducer(request, null),
                    new AbstractCharResponseConsumer<Void>() {

                        @Override
                        protected void start(
                                final HttpResponse response,
                                final ContentType contentType) throws HttpException, IOException {
                            System.out.println(request + "->" + new StatusLine(response));
                        }

                        @Override
                        protected int capacityIncrement() {
                            return Integer.MAX_VALUE;
                        }

                        @Override
                        protected void data(final CharBuffer data, final boolean endOfStream) throws IOException {
                            while (data.hasRemaining()) {
                                System.out.print(data.get());
                            }
                            if (endOfStream) {
                                System.out.println();
                            }
                        }

                        @Override
                        protected Void buildResult() throws IOException {
                            return null;
                        }

                        @Override
                        public void failed(final Exception cause) {
                            System.out.println(request + "->" + cause);
                        }

                        @Override
                        public void releaseResources() {
                        }

                    }, null);
            future.get();
        }
        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }
}
