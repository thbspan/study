package com.test.annotation;

import java.lang.annotation.Repeatable;

/**
 * 定义Repeatable后，可以在同一个对象上重复使用相同注解
 */
@Repeatable(Persons.class)
public @interface Person {

    String role() default "";
}
