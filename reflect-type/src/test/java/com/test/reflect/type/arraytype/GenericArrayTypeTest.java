package com.test.reflect.type.arraytype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GenericArrayTypeTest {

    private int[] nums;

    private String[] strings;
    public <T> T[] getArray() {
        return null;
    }

    public int[] getArray2() {
        return null;
    }

    @Test
    public void testMethod() throws NoSuchMethodException, NoSuchFieldException {
        Type type = getMethodGenericReturnType("getArray");
        Assertions.assertTrue(GenericArrayType.class.isAssignableFrom(type.getClass()) );// true

        type = getFieldType("strings");

        Assertions.assertFalse(GenericArrayType.class.isAssignableFrom(type.getClass()) );// false
        type = getMethodGenericReturnType("getArray2");
        Assertions.assertFalse(GenericArrayType.class.isAssignableFrom(type.getClass()) );// false
    }
    private Type getMethodGenericReturnType(String name) throws NoSuchMethodException {
        Method method = GenericArrayTypeTest.class.getDeclaredMethod(name);
        return method.getGenericReturnType();
    }
    private Type getFieldType(String name) throws NoSuchFieldException {
        return GenericArrayTypeTest.class.getDeclaredField(name).getGenericType();
    }
}
