package com.test.cglib;

import org.junit.jupiter.api.Test;

import com.test.cglib.annotation.Bean;

import net.sf.cglib.core.DebuggingClassWriter;

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
        // 保存cglib生成的字节码
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "target/cglib");
        BeanMethodEnhancer enhancer = new BeanMethodEnhancer();

        // @SuppressWarnings("unchecked")
        // Class<BeanConfig> clazz = (Class<BeanConfig>) enhancer.enhance(BeanConfig.class);
        // BeanConfig config = clazz.newInstance();
        BeanConfig config = (BeanConfig)enhancer.enhanceObject(BeanConfig.class);
        System.out.println(config.t1());
        System.out.println(config.test1());
        System.out.println(config.test2());
        System.out.println(config.test3());
        // 测试拦截私有方法
        System.out.println(config.test4());
        System.out.println(config.test5());
    }
}
