package com.test.reflect.typevariable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.junit.Assert;
import org.junit.Test;

/**
 * 变量类型，泛型中的变量
 */
public class TypeVariableTest<T extends Number & Serializable, V> {

    private T t;
    private V v;

    @Test
    public void testField() throws NoSuchFieldException {
        Field field = TypeVariableTest.class.getDeclaredField("t");
        Type type = field.getGenericType();
        System.out.println(type.getClass());
    }
    @Test
    public void testMethod() throws NoSuchMethodException {
        Method method = TypeVariableTest.class.getDeclaredMethod("getMapper");
        Type returnType = method.getGenericReturnType();
        Assert.assertTrue(TypeVariable.class.isAssignableFrom(returnType.getClass()) );// true
        TypeVariable typeVariable = (TypeVariable) returnType;
        // 获取 T extends Number & Serializable 中 extends后面的内容；没有默认Object
        for (Type bound : typeVariable.getBounds()) {
            System.out.println(bound);
        }
        // 获取泛型的名称 即 K V E等等
        System.out.println(typeVariable.getName());
        // 获取声明该类型变量实体，也就是TypeVariableTest<T>中的TypeVariableTest；
        System.out.println(typeVariable.getGenericDeclaration());
    }

    public <S> S getMapper(){
        return null;
    }

    private T getT() {
        return t;
    }

    private V getV() {
        return v;
    }
}
