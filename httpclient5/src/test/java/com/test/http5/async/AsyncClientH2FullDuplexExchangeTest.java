package com.test.http5.async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.async.MinimalHttpAsyncClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.support.BasicRequestBuilder;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

/**
 * This example demonstrates a full-duplex, streaming HTTP/2 message exchange
 */
public class AsyncClientH2FullDuplexExchangeTest {

    @Test
    public void test() throws InterruptedException {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();
        MinimalHttpAsyncClient client = HttpAsyncClients.createMinimal(HttpVersionPolicy.FORCE_HTTP_2, H2Config.DEFAULT, null, ioReactorConfig);

        client.start();
        BasicHttpRequest request = BasicRequestBuilder.post("https://nghttp2.org/httpbin/post").build();
        BasicRequestProducer requestProducer = new BasicRequestProducer(request,
                new BasicAsyncEntityProducer("stuff", ContentType.TEXT_PLAIN));

        BasicResponseConsumer<String> responseConsumer = new BasicResponseConsumer<>(new StringAsyncEntityConsumer());
        System.out.println("Executing request " + request);
        CountDownLatch latch = new CountDownLatch(1);

        client.execute(new AsyncClientExchangeHandler() {

            @Override
            public void releaseResources() {
                requestProducer.releaseResources();
                responseConsumer.releaseResources();
                latch.countDown();
            }

            @Override
            public void cancel() {
                System.out.println(request + " cancelled");
            }

            @Override
            public void failed(final Exception cause) {
                System.out.println(request + "->" + cause);
            }

            @Override
            public void produceRequest(final RequestChannel channel, final HttpContext context) throws HttpException, IOException {
                requestProducer.sendRequest(channel, context);
            }

            @Override
            public int available() {
                return requestProducer.available();
            }

            @Override
            public void produce(final DataStreamChannel channel) throws IOException {
                requestProducer.produce(channel);
            }

            @Override
            public void consumeInformation(final HttpResponse response,
                                           final HttpContext context) throws HttpException, IOException {
                System.out.println(request + " --1-> " + new StatusLine(response));
            }

            @Override
            public void consumeResponse(final HttpResponse response,
                                        final EntityDetails entityDetails,
                                        final HttpContext context) throws HttpException, IOException {
                System.out.println(request + " --2-> " + new StatusLine(response));
                responseConsumer.consumeResponse(response, entityDetails, context, null);
            }

            @Override
            public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
                responseConsumer.updateCapacity(capacityChannel);
            }

            @Override
            public void consume(final ByteBuffer src) throws IOException {
                responseConsumer.consume(src);
            }

            @Override
            public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
                responseConsumer.streamEnd(trailers);
            }

        });
        latch.await(1, TimeUnit.MINUTES);

        System.out.println("Shutting down");
        client.close(CloseMode.GRACEFUL);
    }
}
