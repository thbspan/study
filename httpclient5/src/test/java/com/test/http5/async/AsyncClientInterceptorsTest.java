package com.test.http5.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.BasicEntityDetails;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

/**
 * This example demonstrates how to insert custom request interceptor and an execution interceptor
 * to the request execution chain.
 */
public class AsyncClientInterceptorsTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();
        CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .addRequestInterceptorFirst(new HttpRequestInterceptor() {
                    private final AtomicLong count = new AtomicLong(0);

                    @Override
                    public void process(HttpRequest request, EntityDetails entity, HttpContext context) throws HttpException, IOException {
                        request.setHeader("request-id", Long.toString(count.incrementAndGet()));
                    }
                })
                .addExecInterceptorAfter(ChainElement.PROTOCOL.name(), "custom", new AsyncExecChainHandler() {
                    @Override
                    public void execute(HttpRequest request, AsyncEntityProducer requestEntityProducer, AsyncExecChain.Scope scope, AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
                        Header idHeader = request.getFirstHeader("request-id");
                        if (idHeader != null && "13".equalsIgnoreCase(idHeader.getValue())) {
                            final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_NOT_FOUND, "Oppsie");
                            final ByteBuffer content = ByteBuffer.wrap("bad luck".getBytes(StandardCharsets.US_ASCII));
                            final AsyncDataConsumer asyncDataConsumer = asyncExecCallback.handleResponse(response,
                                    new BasicEntityDetails(content.remaining(), ContentType.TEXT_PLAIN));
                            asyncDataConsumer.consume(content);
                            asyncDataConsumer.streamEnd(null);
                        } else {
                            chain.proceed(request, requestEntityProducer, scope, asyncExecCallback);
                        }
                    }
                })
                .build();
        client.start();
        String requestUri = "http://httpbin.org/get";
        for (int i = 0; i < 20; i++) {
            final SimpleHttpRequest request = SimpleRequestBuilder.get(requestUri).build();
            System.out.println("Executing request " + request);
            final Future<SimpleHttpResponse> future = client.execute(request,
                    new FutureCallback<SimpleHttpResponse>() {
                        @Override
                        public void completed(final SimpleHttpResponse response) {
                            System.out.println(request + "->" + new StatusLine(response));
                            System.out.println(response.getBody());
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
        }
        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }
}
