package com.test.http5;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.hc.client5.http.impl.io.LenientHttpResponseParser;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.io.HttpConnectionFactory;
import org.apache.hc.core5.http.io.HttpMessageParser;
import org.apache.hc.core5.http.io.HttpMessageParserFactory;
import org.apache.hc.core5.http.io.HttpMessageWriterFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

public class ClientConfigurationTest {

    @Test
    public void test() throws IOException, URISyntaxException, ParseException {
        // response parse
        final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {
            @Override
            public HttpMessageParser<ClassicHttpResponse> create(final Http1Config h1Config) {
                final LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (final ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                return new LenientHttpResponseParser(lineParser, DefaultClassicHttpResponseFactory.INSTANCE, h1Config);
            }
        };
        final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();
        // Create HTTP/1.1 protocol configuration
        final Http1Config h1Config = Http1Config.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();
        final CharCodingConfig connectionConfig = CharCodingConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(StandardCharsets.UTF_8)
                .build();

        final HttpConnectionFactory<ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                h1Config, connectionConfig, requestWriterFactory, responseParserFactory);

        final SSLContext sslcontext = SSLContexts.createSystemDefault();
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();

        // custom dns resolver
        final DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("myhost")) {
                    return new InetAddress[]{InetAddress.getByAddress(new byte[]{127, 0, 0, 1})};
                } else {
                    return super.resolve(host);
                }
            }
        };

        // 连接池设置
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, PoolConcurrencyPolicy.STRICT, PoolReusePolicy.LIFO, TimeValue.ofMinutes(5),
                null, dnsResolver, null);
        final SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 10 sec of inactivity
        connManager.setValidateAfterInactivity(TimeValue.ofSeconds(10));
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 20);
        // 使用自定义cookie存储
        final CookieStore cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(StandardCookieSpec.STRICT)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.NTLM, StandardAuthScheme.DIGEST))
                .setProxyPreferredAuthSchemes(Collections.singletonList(StandardAuthScheme.BASIC))
                .build();

        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCredentialsProvider(credentialsProvider)
                // .setProxy(new HttpHost("myproxy", 8080))
                .setDefaultRequestConfig(defaultRequestConfig)
                .build()) {
            final HttpGet httpget = new HttpGet("http://httpbin.org/get");
            final RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig)
                    .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                    .setConnectTimeout(Timeout.ofSeconds(5))
                    // .setProxy(new HttpHost("myotherproxy", 8080))
                    .build();
            httpget.setConfig(requestConfig);

            final HttpClientContext context = HttpClientContext.create();
            // Contextual attributes set the local context level will take
            // precedence over those set at the client level.
            context.setCookieStore(cookieStore);
            context.setCredentialsProvider(credentialsProvider);
            System.out.println("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            try (final CloseableHttpResponse response = httpclient.execute(httpget, context)) {
                System.out.println("----------------------------------------");
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                System.out.println(EntityUtils.toString(response.getEntity()));

                // Once the request has been executed the local context can
                // be used to examine updated state and various objects affected
                // by the request execution.

                // Last executed request
                System.out.println("request:" + context.getRequest());
                // Execution route
                System.out.println("route:" + context.getHttpRoute());
                // Auth exchanges
                System.out.println("exchanges:" + context.getAuthExchanges());
                // Cookie origin
                System.out.println("origin:" + context.getCookieOrigin());
                // Cookie spec used
                System.out.println("cookie spec:" + context.getCookieSpec());
                // User security token
                System.out.println("token:" + context.getUserToken());
            }
        }
    }
}
