package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.Test;

public class ClientAbortMethodTest {

    @Test
    public void test() throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            final HttpGet httpget = new HttpGet("http://httpbin.org/get");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            try (final CloseableHttpResponse response = httpClient.execute(httpget)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                // Do not feel like reading the response body
                // Call cancel on the request object
                httpget.cancel();
            }
        }
    }
}
