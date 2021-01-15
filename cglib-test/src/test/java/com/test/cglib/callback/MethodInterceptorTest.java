package com.test.cglib.callback;

import org.junit.jupiter.api.Test;

import com.test.cglib.Example;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodInterceptorTest {

    @Test
    public void test() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            try {
                System.out.println("before");
                return proxy.invokeSuper(obj, args);
            } finally {
                System.out.println("after");
            }
        });
        Example example = (Example)enhancer.create();

        assertEquals("hello world", example.test(null));
    }
}
