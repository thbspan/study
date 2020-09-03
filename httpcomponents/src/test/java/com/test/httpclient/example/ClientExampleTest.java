package com.test.httpclient.example;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class ClientExampleTest {

    @Test
    public void testGet() throws IOException {
        System.out.println(ClientExample.get("http://www.baidu.com"));
    }
}
