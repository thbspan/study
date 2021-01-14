package com.test.cglib.callback;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;

public interface ConditionalCallback extends Callback {
    boolean isMatch(Method candidateMethod);
}
