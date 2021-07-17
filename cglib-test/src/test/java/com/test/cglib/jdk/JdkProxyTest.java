package com.test.cglib.jdk;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;

import sun.misc.ProxyGenerator;

public class JdkProxyTest {

    @Test
    public void test() {
        Subject realSubject = new RealSubject();
        InvocationHandler handler = new InvocationHandlerImpl(realSubject);
        Subject subject = (Subject) Proxy.newProxyInstance(Subject.class.getClassLoader(), realSubject.getClass().getInterfaces(), handler);

        System.out.println("proxy class=" + subject.getClass());
        System.out.println(subject.sayHello("peter"));
        System.out.println(subject.sayGoodBye("peter"));
    }

    @Test
    public void testGenerateProxyClass() throws IOException {
        String className = "$Proxy4";
        byte[] classFile = ProxyGenerator.generateProxyClass(className, new Class[]{RealSubject.class});
        try (FileOutputStream fos = new FileOutputStream(String.format("target/%s.class", className))) {
            fos.write(classFile);
            fos.flush();
        }
    }
}
