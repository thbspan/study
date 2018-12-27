package com.test.reflect.type.parameterizedtype;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ParameterizedTypeTest {

    private List list;
    private List<String> list2;
    private List<?> list3;

    @Test
    public void testList() throws NoSuchFieldException {
        testFieldInfo("list");
        // class java.lang.Class
    }
    @Test
    public void testList2() throws NoSuchFieldException {
        testFieldInfo("list2");
        // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
    }
    @Test
    public void testList3() throws NoSuchFieldException {
        testFieldInfo("list3");
        // class sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
    }
    private void testFieldInfo(String name) throws NoSuchFieldException {
        Field field = ParameterizedTypeTest.class.getDeclaredField(name);
        Type type = field.getGenericType();
        System.out.println(type.getClass());
        Assert.assertTrue(type instanceof ParameterizedType);
    }
}
