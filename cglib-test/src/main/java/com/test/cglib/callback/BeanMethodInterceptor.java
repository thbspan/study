package com.test.cglib.callback;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.test.cglib.annotation.Bean;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;


public class BeanMethodInterceptor implements MethodInterceptor, ConditionalCallback {
    private final ConcurrentMap<Method, Object> factory = new ConcurrentHashMap<>();

    @Override
    public boolean isMatch(Method candidateMethod) {
        return candidateMethod.isAnnotationPresent(Bean.class);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return factory.computeIfAbsent(method, m -> {
            try {
                return methodProxy.invokeSuper(obj, args);
            } catch (Throwable throwable) {
                throw new IllegalStateException(throwable);
            }
        });
    }
}
