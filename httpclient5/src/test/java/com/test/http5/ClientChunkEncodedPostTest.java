package com.test.http5;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Example how to use unbuffered chunk-encoded POST request.
 */
public class ClientChunkEncodedPostTest {

    @ParameterizedTest
    @ValueSource(strings = "pom.xml")
    public void test(String fileName) throws URISyntaxException, IOException, ParseException {
        try (final CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost httppost = new HttpPost("http://httpbin.org/post");

            final File file = new File(fileName);

            final InputStreamEntity reqEntity = new InputStreamEntity(
                    new FileInputStream(file), -1, ContentType.APPLICATION_OCTET_STREAM);
            // It may be more appropriate to use FileEntity class in this particular
            // instance but we are using a more generic InputStreamEntity to demonstrate
            // the capability to stream out data from any arbitrary source
            //
            // FileEntity entity = new FileEntity(file, "binary/octet-stream");

            httppost.setEntity(reqEntity);

            System.out.println("Executing request " + httppost.getMethod() + " " + httppost.getUri());
            try (final CloseableHttpResponse response = httpClient.execute(httppost)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
