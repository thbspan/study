package com.test.cglib;

import com.test.cglib.annotation.Bean;
import com.test.cglib.callback.BeanMethodInterceptor;

import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

/**
 * 模拟spring实现{@link Bean}注解增强
 */
public class BeanMethodEnhancer {
    private static final Callback[] CALLBACKS = new Callback[]{
            new BeanMethodInterceptor(),
            NoOp.INSTANCE
    };

    private static final ConditionalCallbackFilter CALLBACK_FILTER = new ConditionalCallbackFilter(CALLBACKS);

    public Class<?> enhanceClass(Class<?> beanClass) {
        if (EnhancedConfiguration.class.isAssignableFrom(beanClass)) {
            return beanClass;
        }

        Enhancer enhancer = createEnhancer(beanClass);
        Class<?> subclass = enhancer.createClass();
        Enhancer.registerStaticCallbacks(subclass, CALLBACKS);
        return subclass;
    }

    public Object enhanceObject(Class<?> beanClass) {
        if (EnhancedConfiguration.class.isAssignableFrom(beanClass)) {
            return beanClass;
        }

        Enhancer enhancer = createEnhancer(beanClass);
        enhancer.setCallbacks(CALLBACKS);
        return enhancer.create();
    }

    private Enhancer createEnhancer(Class<?> beanClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanClass);
        enhancer.setInterfaces(new Class<?>[]{EnhancedConfiguration.class});
        enhancer.setUseFactory(false);
        enhancer.setNamingPolicy(BeanMethodNamingPolicy.INSTANCE);
        enhancer.setStrategy(DefaultGeneratorStrategy.INSTANCE);
        enhancer.setCallbackFilter(CALLBACK_FILTER);
        enhancer.setCallbackTypes(CALLBACK_FILTER.getCallbackTypes());
        return enhancer;
    }
}
