package com.test.reflect.clazz;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ClazzTest {
    private int a;

    private int[] nums;
    private String string;
    private String[] strings;
    private List list;
    private List<String> list2;
    private List<?> list3;
    /**
     * 原始类型
     */
    @Test
    public void testPrimitiveType() throws NoSuchFieldException {
        testFieldInfo("a");// true
        testFieldInfo("nums");// true
    }

    /**
     * 没有泛型的对象
     */
    @Test
    public void testObject() throws NoSuchFieldException {
        testFieldInfo("string");// true
        testFieldInfo("strings");// true
    }
    @Test
    public void testList() throws NoSuchFieldException {
        testFieldInfo("list");
    }
    @Test
    public void testList2() throws NoSuchFieldException {
        testFieldInfo("list2");
    }
    @Test
    public void testList3() throws NoSuchFieldException {
        testFieldInfo("list3");
    }
    private void testFieldInfo(String name) throws NoSuchFieldException {
        Field field = ClazzTest.class.getDeclaredField(name);
        Type type = field.getGenericType();
        System.out.println(type.getClass());
        Assert.assertTrue(type instanceof Class);
    }
}
