package com.test.httpclient.example;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HeaderElement;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicHttpResponse;

public class URIExample {
    public static void main(String[] args) throws URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("dubbo")
                .setHost("127.0.0.1")
                .setPath("/search")
//                .setParameter("q", "中")
                .setParameter("q", "%E4%B8%AD")
                .build();
        System.out.println(uri);

        BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");

        response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
        response.addHeader("Set-Cookie", "c2=b; path=\"/\"; domain=\"localhost\"");
        response.addHeader("refer", "www.baidu.com");

        BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Set-Cookie"));

        while (it.hasNext()) {
            HeaderElement elem = it.nextElement();
            System.out.println(elem.getName() + " = " + elem.getValue());
            NameValuePair[] params = elem.getParameters();
            System.out.println("params length:" + params.length);
            for (NameValuePair param : params) {
                System.out.println(" " + param);
            }
        }
    }
}
