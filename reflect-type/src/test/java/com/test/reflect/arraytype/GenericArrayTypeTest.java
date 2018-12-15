package com.test.reflect.arraytype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(GenericArrayType.class.isAssignableFrom(type.getClass()) );// true

        type = getFieldType("strings");

        Assert.assertTrue(GenericArrayType.class.isAssignableFrom(type.getClass()) );// false
        type = getMethodGenericReturnType("getArray2");
        Assert.assertTrue(GenericArrayType.class.isAssignableFrom(type.getClass()) );// false
    }
    private Type getMethodGenericReturnType(String name) throws NoSuchMethodException {
        Method method = GenericArrayTypeTest.class.getDeclaredMethod(name);
        return method.getGenericReturnType();
    }
    private Type getFieldType(String name) throws NoSuchFieldException {
        return GenericArrayTypeTest.class.getDeclaredField(name).getGenericType();
    }
}
