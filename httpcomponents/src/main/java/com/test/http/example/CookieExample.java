package com.test.http.example;

import java.io.IOException;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.CookieSpecRegistries;
import org.apache.http.impl.client.HttpClients;

public class CookieExample {
    public static void main(String[] args) throws IOException {
        RequestConfig config = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();

        // copy 复制属性
        RequestConfig localConfig = RequestConfig.copy(config)
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        HttpGet httpGet = new HttpGet("http://www.baidu.com");
        httpGet.setConfig(localConfig);
        System.out.println(httpClient.execute(httpGet));

        Registry<CookieSpecProvider> r = CookieSpecRegistries.createDefaultBuilder()
                .register("null-spec", context -> null)
                .build();

        config = RequestConfig.custom()
                .setCookieSpec("null-spec")
                .build();
        httpClient = HttpClients.custom()
                .setDefaultCookieSpecRegistry(r)
                .setDefaultRequestConfig(config)
                .build();
        System.out.println(httpClient);

    }
}
