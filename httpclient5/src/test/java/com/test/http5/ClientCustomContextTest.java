package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;

/**
 * This example demonstrates the use of a local HTTP context populated with
 * custom attributes.
 */
public class ClientCustomContextTest {

    @Test
    public void test() throws URISyntaxException, IOException {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            // Create a local instance of cookie store
            final CookieStore cookieStore = new BasicCookieStore();

            // Create local HTTP context
            final HttpClientContext localContext = HttpClientContext.create();
            // Bind custom cookie store to the local context
            localContext.setCookieStore(cookieStore);

            final HttpGet httpget = new HttpGet("http://httpbin.org/cookies");
            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            // Pass local context as a parameter
            try (final CloseableHttpResponse response = httpclient.execute(httpget, localContext)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                final List<Cookie> cookies = cookieStore.getCookies();
                for (Cookie cookie : cookies) {
                    System.out.println("Local cookie: " + cookie);
                }
                EntityUtils.consume(response.getEntity());
            }
        }
    }
}
