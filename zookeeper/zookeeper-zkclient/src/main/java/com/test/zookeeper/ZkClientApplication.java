package com.test.zookeeper;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class ZkClientApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ZkClientApplication.class);
        builder.run(args);
    }
}
