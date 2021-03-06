package com.test.reflect.type.parameterizedtype.wildcardtype;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WildcardTypeTest {

    public void wildType(Class<? extends Number> clazz) {
    }

    @Test
    public void testMethodParam() throws NoSuchMethodException {
        Method method = this.getClass().getMethod("wildType", Class.class);
        Type[] types = method.getGenericParameterTypes();
        for (Type parameterizedType : types) {
            Assertions.assertTrue(parameterizedType instanceof ParameterizedType); // true
            Type[] actualTypeArgument = ((ParameterizedType)parameterizedType).getActualTypeArguments();
            for (Type type : actualTypeArgument) {
                Assertions.assertTrue(type instanceof WildcardType);
                WildcardType wildcardType = (WildcardType) type;
                Type[] lowerBounds = wildcardType.getLowerBounds();
                if (null != lowerBounds) {
                    for (Type lowerBound : lowerBounds) {
                        System.out.println(lowerBound);
                    }
                }
                Type[] upperBounds = wildcardType.getUpperBounds();
                for (Type upperBound : upperBounds) {
                    System.out.println(upperBound);
                }
            }
        }
    }
}
