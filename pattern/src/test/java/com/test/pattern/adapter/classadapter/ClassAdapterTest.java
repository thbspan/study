package com.test.pattern.adapter.classadapter;

import org.junit.jupiter.api.Test;

public class ClassAdapterTest {

    @Test
    public void test(){
        Target target = new ClassAdapter();
        target.output5V();
    }
}
