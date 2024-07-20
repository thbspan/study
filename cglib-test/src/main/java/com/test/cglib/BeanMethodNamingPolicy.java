package com.test.cglib;

import org.springframework.cglib.core.DefaultNamingPolicy;

public class BeanMethodNamingPolicy extends DefaultNamingPolicy {
    public static final BeanMethodNamingPolicy INSTANCE = new BeanMethodNamingPolicy();

    private BeanMethodNamingPolicy() {
    }

    @Override
    protected String getTag() {
        return "ByBeanMethod";
    }
}
