package com.test.http5.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.async.methods.SimpleRequestProducer;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.entity.DigestingEntityProducer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

/**
 * This example demonstrates how to use a custom execution interceptor
 * to add trailers to all outgoing request enclosing an entity.
 */
public class AsyncClientMessageTrailersTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .addExecInterceptorAfter(ChainElement.PROTOCOL.name(), "custom", (request, entityProducer, scope, chain, asyncExecCallback) -> {
                    // Send MD5 hash in a trailer by decorating the original entity producer
                    chain.proceed(request,
                            entityProducer != null ? new DigestingEntityProducer("MD5", entityProducer) : null,
                            scope,
                            asyncExecCallback);
                })
                .build();

        client.start();

        final SimpleHttpRequest request = SimpleRequestBuilder.post("https://nghttp2.org/httpbin/post")
                .setBody("some stuff", ContentType.TEXT_PLAIN)
                .build();

        System.out.println("Executing request " + request);
        final Future<SimpleHttpResponse> future = client.execute(
                SimpleRequestProducer.create(request),
                SimpleResponseConsumer.create(),
                new FutureCallback<SimpleHttpResponse>() {

                    @Override
                    public void completed(final SimpleHttpResponse response) {
                        System.out.println(request + " -> " + new StatusLine(response));
                        SimpleBody responseBody = response.getBody();
                        System.out.println(responseBody);
                        System.out.println(responseBody.getBodyText());
                        for (Header header : response.getHeaders()) {
                            System.out.println(header);
                        }
                    }

                    @Override
                    public void failed(final Exception ex) {
                        System.out.println(request + "->" + ex);
                    }

                    @Override
                    public void cancelled() {
                        System.out.println(request + " cancelled");
                    }

                });
        future.get();

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }
}
