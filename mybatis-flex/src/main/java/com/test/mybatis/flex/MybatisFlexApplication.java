package com.test.mybatis.flex;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.test.mybatis.flex.mapper")
public class MybatisFlexApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisFlexApplication.class, args);
    }
}
