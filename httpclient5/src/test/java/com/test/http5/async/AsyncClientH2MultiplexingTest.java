package com.test.http5.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

/**
 * This example demonstrates concurrent (multiplexed) execution of multiple
 * HTTP/2 message exchanges.
 */
public class AsyncClientH2MultiplexingTest {

    @Test
    public void test() throws ExecutionException, InterruptedException, TimeoutException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        MinimalHttpAsyncClient client = HttpAsyncClients.createMinimal(
                HttpVersionPolicy.FORCE_HTTP_2, H2Config.DEFAULT, null, ioReactorConfig);

        client.start();

        HttpHost target = new HttpHost("https", "nghttp2.org");
        Future<AsyncClientEndpoint> leaseFuture = client.lease(target, null);
        AsyncClientEndpoint endpoint = leaseFuture.get(30, TimeUnit.SECONDS);

        try {
            String[] requestUris = new String[]{"/httpbin/ip", "/httpbin/user-agent", "/httpbin/headers"};

            CountDownLatch latch = new CountDownLatch(requestUris.length);

            for (final String requestUri : requestUris) {
                final SimpleHttpRequest request = SimpleRequestBuilder.get()
                        .setHttpHost(target)
                        .setPath(requestUri)
                        .build();

                System.out.println("Executing request " + request);
                endpoint.execute(SimpleRequestProducer.create(request),
                        SimpleResponseConsumer.create(),
                        new FutureCallback<SimpleHttpResponse>() {
                            @Override
                            public void completed(final SimpleHttpResponse response) {
                                latch.countDown();
                                System.out.println(request + " -> " + new StatusLine(response));
                                System.out.println(response.getBody());
                            }

                            @Override
                            public void failed(final Exception ex) {
                                latch.countDown();
                                System.out.println(request + " -> " + ex);
                            }

                            @Override
                            public void cancelled() {
                                latch.countDown();
                                System.out.println(request + " cancelled");
                            }
                        });
            }
            latch.await();
        } finally {
            endpoint.releaseAndReuse();
        }
        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }
}
