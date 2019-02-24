package com.test.mybatis.cursor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.test.mybatis.cursor.service.UserService;

@SpringBootApplication
public class MybatisCursorApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(MybatisCursorApplication.class, args);
    }

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) {
//        userService.testInit();
        userService.testCursor();
    }

}
