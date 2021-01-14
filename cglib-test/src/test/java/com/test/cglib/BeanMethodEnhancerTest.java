package com.test.cglib;

import org.junit.jupiter.api.Test;

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
    }
}
