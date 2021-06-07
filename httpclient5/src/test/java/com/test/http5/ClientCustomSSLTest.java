package com.test.http5;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

public class ClientCustomSSLTest {

    @Test
    public void test() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, ParseException, URISyntaxException {
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> {
                    final X509Certificate cert = chain[0];
                    return "CN=httpbin.org".equalsIgnoreCase(cert.getSubjectDN().getName());
                })
                .build();
        // Allow TLSv1.2 protocol only
        final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .setTlsVersions(TLS.V_1_2)
                .build();
        final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .build()) {
            final HttpGet httpget = new HttpGet("https://httpbin.org/");

            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());

            final HttpClientContext clientContext = HttpClientContext.create();
            try (CloseableHttpResponse response = httpclient.execute(httpget, clientContext)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));

                final SSLSession sslSession = clientContext.getSSLSession();
                if (sslSession != null) {
                    System.out.println("SSL protocol " + sslSession.getProtocol());
                    System.out.println("SSL cipher suite " + sslSession.getCipherSuite());
                }
            }
        }
    }
}
