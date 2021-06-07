package com.test.http5;

import java.io.File;
import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Example how to use multipart/form encoded POST request.
 */
public class ClientMultipartFormPostTest {

    @ParameterizedTest
    @ValueSource(strings = "pom.xml")
    public void test(String fileName) throws IOException {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost httppost = new HttpPost("http://httpbin.org/post");

            final FileBody bin = new FileBody(new File(fileName));
            final StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
            final HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("bin", bin)
                    .addPart("comment", comment)
                    .build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost);
            try (final CloseableHttpResponse response = httpClient.execute(httppost)) {
                System.out.println("----------------------------------------");
                System.out.println(response);
                final HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            }
        }
    }
}
