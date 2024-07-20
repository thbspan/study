package com.test.cglib;

import java.lang.reflect.Method;

import com.test.cglib.callback.ConditionalCallback;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;

public class ConditionalCallbackFilter implements CallbackFilter {
    private final Callback[] callbacks;
    private final Class<?>[] callbackTypes;

    public ConditionalCallbackFilter(Callback[] callbacks) {
        this.callbacks = callbacks;
        this.callbackTypes = new Class<?>[callbacks.length];
        for (int i = 0; i < callbacks.length; i++) {
            this.callbackTypes[i] = callbacks[i].getClass();
        }
    }

    @Override
    public int accept(Method method) {
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (!(callback instanceof ConditionalCallback) || ((ConditionalCallback) callback).isMatch(method)) {
                return i;
            }
        }
        throw new IllegalStateException("No callback available for method " + method.getName());
    }

    public Class<?>[] getCallbackTypes() {
        return callbackTypes;
    }
}
