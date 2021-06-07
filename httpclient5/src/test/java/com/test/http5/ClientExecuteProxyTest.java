package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;

public class ClientExecuteProxyTest {

    @Test
    public void test() throws IOException, ParseException, URISyntaxException {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpHost proxy = new HttpHost("http", "127.0.0.1", 8080);
            final RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            final HttpGet request = new HttpGet("/get");
            request.setConfig(config);
            System.out.println("Executing request " + request.getMethod() + " " + request.getUri() +
                    " via " + proxy);

            final HttpHost target = new HttpHost("https", "httpbin.org", 443);
            try (final CloseableHttpResponse response = httpClient.execute(target, request)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
