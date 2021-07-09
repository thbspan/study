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

    @Bean
    public String test4() {
        return "test4" + t11();
    }

    @Bean
    public String test5() {
        return "test5" + t11();
    }

    /**
     * 私有方法测试
     */
    @Bean
    private T1 t11() {
        return new T1();
    }
}
