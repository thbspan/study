package com.test.cglib.callback;

import org.junit.jupiter.api.Test;

import com.test.cglib.BeanMethodNamingPolicy;
import com.test.cglib.Example;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.FixedValue;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 返回固定值测试类
 */
public class FixedValueTest {
    @Test
    public void test() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallback((FixedValue) () -> "hello cglib!");
        Example example = (Example) enhancer.create();

        assertEquals("hello cglib!", example.toString());
        assertEquals("hello cglib!", example.test(null));
        //Object#hashCode
        assertThrows(ClassCastException.class, example::hashCode);
    }

    @Test
    public void testCreateSubClass() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallbackTypes(new Class<?>[]{FixedValue.class});
        enhancer.setNamingPolicy(BeanMethodNamingPolicy.INSTANCE);
        @SuppressWarnings("unchecked")
        Class<Example> subclass = (Class<Example>) enhancer.createClass();
        Enhancer.registerStaticCallbacks(subclass, new Callback[]{(FixedValue) () -> "hello cglib!"});
        Example example = (Example) enhancer.create();
        assertEquals("hello cglib!", example.test(null));
    }
}
