package com.test.pattern.adapter.objectadapter;

import org.junit.jupiter.api.Test;

import com.test.pattern.adapter.classadapter.Source;
import com.test.pattern.adapter.classadapter.Target;

public class ObjectAdapterTest {

    @Test
    public void test() {
        Target target = new ObjectAdapter(new Source());
        target.output5V();
    }
}
