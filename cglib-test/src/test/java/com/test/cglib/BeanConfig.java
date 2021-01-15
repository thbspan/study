package com.test.cglib;

import com.test.cglib.annotation.Bean;

public class BeanConfig {

    @Bean
    public T1 t1() {
        return new T1();
    }

    static class T1 {

    }

    @Bean
    public String test1() {
        return "test1" + t1();
    }

    @Bean
    public String test2() {
        return "test2" + t1();
    }

    public String test3() {
        return "test3" + t1();
    }

    public String test4() {
        return "test4";
    }
}
