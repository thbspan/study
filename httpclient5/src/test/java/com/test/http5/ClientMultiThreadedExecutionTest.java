package com.test.http5;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

/**
 * An example that performs GETs from multiple threads.
 */
public class ClientMultiThreadedExecutionTest {

    @Test
    public void test() throws IOException, InterruptedException {
        final PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        try (final CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build()) {
            final String[] urisToGet = {
                    "http://hc.apache.org/",
                    "http://hc.apache.org/httpcomponents-core-ga/",
                    "http://hc.apache.org/httpcomponents-client-ga/",
            };
            final GetThread[] threads = new GetThread[urisToGet.length];
            for (int i = 0; i < threads.length; i++) {
                final HttpGet httpget = new HttpGet(urisToGet[i]);
                threads[i] = new GetThread(httpClient, httpget, i + 1);
            }

            // start the threads
            for (final GetThread thread : threads) {
                thread.start();
            }

            // join the threads
            for (final GetThread thread : threads) {
                thread.join();
            }
        }
    }

    static class GetThread extends Thread {
        private final CloseableHttpClient httpClient;
        private final HttpContext context;
        private final HttpGet httpget;
        private final int id;

        public GetThread(final CloseableHttpClient httpClient, final HttpGet httpget, final int id) {
            this.httpClient = httpClient;
            this.context = new BasicHttpContext();
            this.httpget = httpget;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                System.out.println(id + " - about to get something from " + httpget.getUri());
                try (CloseableHttpResponse response = httpClient.execute(httpget, context)) {
                    System.out.println(id + " - get executed");
                    // get the response body as an array of bytes
                    final HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        final byte[] bytes = EntityUtils.toByteArray(entity);
                        System.out.println(id + " - " + bytes.length + " bytes read");
                    }
                }
            } catch (final Exception e) {
                System.out.println(id + " - error: " + e);
            }
        }
    }
}
