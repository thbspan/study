package com.test.httpclient.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class ClientExample {
    private static final int DEFAULT_TIMEOUT = 30000;
    private static final String DEFAULT_CHARSET = "UTF-8";

    private static final CloseableHttpClient HTTP_CLIENT;

    private static final String USER_AGENT_CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36";
    private static final RequestConfig DEFAULT_REQUEST_CONFIG = RequestConfig.custom()
            .setConnectionRequestTimeout(DEFAULT_TIMEOUT)
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setSocketTimeout(DEFAULT_TIMEOUT)
            .setCircularRedirectsAllowed(true)
            .setMaxRedirects(100)
            .build();

    static {
        try {
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("http", PlainConnectionSocketFactory.getSocketFactory())
                            .register("https", createSSLConnectionSocketFactory())
                            .build(), null, null, null, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
            connectionManager.setMaxTotal(500);
            connectionManager.setDefaultMaxPerRoute(100);
            connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(DEFAULT_TIMEOUT).build());

            HTTP_CLIENT = HttpClients.custom()
                    .setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG)
                    .setConnectionManager(connectionManager)
                    .evictExpiredConnections()
                    .evictIdleConnections(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .setRedirectStrategy(new LaxRedirectStrategy())
//                    .setServiceUnavailableRetryStrategy(new DefaultServiceUnavailableRetryStrategy())
                    .setUserAgent(USER_AGENT_CHROME)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(2, true))
//                    .setContentDecoderRegistry()
                    .build();
            // shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    HTTP_CLIENT.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, "close-http-client"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        RequestConfig config = RequestConfig.copy(DEFAULT_REQUEST_CONFIG)
                // 设置代理
                .setProxy(new HttpHost("127.0.0.1", 8888))
                .build();

        httpGet.setConfig(config);

        final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        // 设置代理用户名密码
        credentialsProvider.setCredentials(new AuthScope("127.0.0.1", 8888),
                new UsernamePasswordCredentials("user", "passcode"));

        final HttpClientContext httpClientContext = HttpClientContext.create();
        httpClientContext.setCredentialsProvider(credentialsProvider);

        try (CloseableHttpResponse httpResponse = HTTP_CLIENT.execute(httpGet, httpClientContext)) {
            return EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static ConnectionSocketFactory createSSLConnectionSocketFactory() throws Exception {
        SSLContext sslContext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
        sslContext.init(null, new TrustManager[]{new InsecureTrustManager()}, null);
        return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    }
}
