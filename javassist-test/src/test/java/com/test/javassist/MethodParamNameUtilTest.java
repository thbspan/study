package com.test.javassist;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MethodParamNameUtilTest {

    @Test
    void testGetMethodParamNames() {
        String[] paramNames = MethodParamNameUtil.getMethodParamNames(Demo.class, "test", int.class, Integer.class);

        Assertions.assertArrayEquals(new String[]{"a", "b"}, paramNames);
    }
}
