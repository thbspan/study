package com.test.cglib.callback;

import com.test.cglib.BeanMethodNamingPolicy;
import com.test.cglib.Example;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.FixedValue;
import org.springframework.cglib.proxy.NoOp;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallbackFilterTest {
    private final Callback[] callbacks = new Callback[]{NoOp.INSTANCE, (FixedValue) () -> "hello cglib!"};
    private final CallbackFilter callbackFilter = method -> {
        if (method.getName().equals("test")) {
            return 1;
        } else {
            return 0;
        }
    };

    @Test
    public void test() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallbacks(callbacks);
        enhancer.setCallbackFilter(callbackFilter);
        Example example = (Example) enhancer.create();
        assertEquals("hello cglib!", example.test(null));
        assertEquals("hello world-1", example.test1(null));
    }

    @Test
    public void testCreateSubClass() throws IllegalAccessException, InstantiationException {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallbackFilter(callbackFilter);
        enhancer.setNamingPolicy(BeanMethodNamingPolicy.INSTANCE);
        Class<?>[] callbackTypes = new Class<?>[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            callbackTypes[i] = callbacks[i].getClass();
        }
        enhancer.setCallbackTypes(callbackTypes);
        @SuppressWarnings("unchecked")
        Class<Example> subclass = (Class<Example>) enhancer.createClass();
        Enhancer.registerStaticCallbacks(subclass, callbacks);
        System.out.println(subclass.getName());
        Example example = subclass.newInstance();
        assertEquals("hello cglib!", example.test(null));
        assertEquals("hello world-1", example.test1(null));
    }
}
