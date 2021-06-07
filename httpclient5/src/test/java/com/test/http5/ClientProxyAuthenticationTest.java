package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;

/**
 * A simple example showing execution of an HTTP request over a secure connection tunneled through an authenticating proxy.
 */
public class ClientProxyAuthenticationTest {

    @Test
    public void test() throws URISyntaxException, IOException, ParseException {
        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("localhost", 8888),
                new UsernamePasswordCredentials("squid", "squid".toCharArray()));

        credentialsProvider.setCredentials(new AuthScope("httpbin.org", 80),
                new UsernamePasswordCredentials("user", "passwd".toCharArray()));
        try (final CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider).build()) {
            final HttpHost target = new HttpHost("http", "httpbin.org", 80);
            final HttpHost proxy = new HttpHost("localhost", 8888);

            final RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            final HttpGet httpget = new HttpGet("/basic-auth/user/passwd");
            httpget.setConfig(config);

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri() +
                    " via " + proxy);

            try (final CloseableHttpResponse response = httpClient.execute(target, httpget)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
