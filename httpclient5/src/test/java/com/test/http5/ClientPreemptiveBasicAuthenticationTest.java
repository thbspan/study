package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;

/**
 * An example of how HttpClient can be customized to authenticate
 * preemptively using Basic scheme.
 * <b/>
 * Generally, preemptive authentication can be considered less
 * secure than a response to an authentication challenge
 * and therefore discouraged.
 */
public class ClientPreemptiveBasicAuthenticationTest {

    @Test
    public void test() throws IOException, URISyntaxException, ParseException {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Generate Basic scheme object and add it to the local auth cache
            final BasicScheme basicAuth = new BasicScheme();
            basicAuth.initPreemptive(new UsernamePasswordCredentials("user", "passwd".toCharArray()));

            final HttpHost target = new HttpHost("http", "httpbin.org", 80);

            // Add AuthCache to the execution context
            final HttpClientContext localContext = HttpClientContext.create();
            localContext.resetAuthExchange(target, basicAuth);

            final HttpGet httpget = new HttpGet("http://httpbin.org/hidden-basic-auth/user/passwd");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            try (final CloseableHttpResponse response = httpClient.execute(httpget, localContext)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
