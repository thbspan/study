package com.test.http5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.classic.ProxyClient;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Proxy tunnel demo
 */
class ProxyTunnelTest {

    @Test
    void test() throws HttpException, IOException {
        final ProxyClient proxyClient = new ProxyClient();
        final HttpHost target = new HttpHost("www.baidu.com", 80);
        final HttpHost proxy = new HttpHost("localhost", 3128);
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("user", "pwd".toCharArray());

        try (final Socket socket = proxyClient.tunnel(proxy, target, credentials)) {
            final Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1);
            out.write("GET / HTTP/1.1\r\n");
            out.write("Host: " + target.toHostString() + "\r\n");
            out.write("Agent: whatever\r\n");
            out.write("Connection: close\r\n");
            out.write("\r\n");
            out.flush();
            final BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    @Test
    void test2() throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder("https://asset.test.myspacex.cn");
        uriBuilder.setPath("/file/proxy");
        uriBuilder.setParameter("file_id", "3434");
        // set会覆盖相同的参数
        uriBuilder.setParameter("file_id", "4568");
        URI uri = uriBuilder.build();
        System.out.println(uri);
        Assertions.assertEquals("file_id=4568", uri.getQuery());
    }
}
