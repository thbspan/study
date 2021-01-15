package com.test.cglib.callback;

import org.junit.jupiter.api.Test;

import com.test.cglib.Example;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 不执行任何操作测试类
 */
public class NoOperationTest {
    @Test
    public void test() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Example.class);
        enhancer.setCallback(NoOp.INSTANCE);
        Example example = (Example)enhancer.create();

        assertEquals("hello world", example.test(null));
    }
}
