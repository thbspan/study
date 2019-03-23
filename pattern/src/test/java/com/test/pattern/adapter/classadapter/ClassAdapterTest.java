package com.test.pattern.adapter.classadapter;

import org.junit.Test;

public class ClassAdapterTest {

    @Test
    public void test(){
        Target target = new ClassAdapter();
        target.output5V();
    }
}
