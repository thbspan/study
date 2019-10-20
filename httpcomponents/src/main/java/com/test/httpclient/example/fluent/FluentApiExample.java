package com.test.httpclient.example.fluent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

public class FluentApiExample {

    public static void main(String[] args) throws IOException {
        System.out.println(Request.Get("http://www.baidu.com")
                .connectTimeout(1000)
                .socketTimeout(1000)
                .execute()
                .returnContent().asString(StandardCharsets.UTF_8));

        System.out.println(Request.Post("http://www.baidu.com")
                .useExpectContinue()
                .version(HttpVersion.HTTP_1_1)
                .bodyString("Important text", ContentType.DEFAULT_TEXT)
                .execute()
                .returnContent()
                .asString(StandardCharsets.UTF_8));
    }
}
