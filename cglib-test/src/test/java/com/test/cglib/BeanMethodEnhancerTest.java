package com.test.cglib;

import org.junit.jupiter.api.Test;

import com.test.cglib.annotation.Bean;

/**
 * {@link Bean}注解增强测试类
 */
public class BeanMethodEnhancerTest {

    @Test
    public void normalTest() {
        BeanConfig config = new BeanConfig();
        System.out.println(config.t1());
        System.out.println(config.test1());
        System.out.println(config.test2());
    }

    @Test
    public void proxyTest() throws IllegalAccessException, InstantiationException {
        BeanMethodEnhancer enhancer = new BeanMethodEnhancer();
        @SuppressWarnings("unchecked")
        Class<BeanConfig> clazz = (Class<BeanConfig>) enhancer.enhance(BeanConfig.class);
        BeanConfig config = clazz.newInstance();
        System.out.println(config.t1());
        System.out.println(config.test1());
        System.out.println(config.test2());
        System.out.println(config.test3());
        System.out.println(config.test4());
    }
}
