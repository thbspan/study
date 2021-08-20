package com.test.arthas.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class WatchReTransformer implements ClassFileTransformer {
    private boolean watch = false;
    private String sessionId;
    private String watchClassName;
    private byte[] originalBytes;

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (className == null || className.isEmpty()) {
            return classFileBuffer;
        }
        String name = className.replace('/', '.');

        if (!name.equals(watchClassName)) {
            return classFileBuffer;
        }

        try {
            if (watch) {
                originalBytes = classFileBuffer;
                ClassPool cp = ClassPool.getDefault();
                cp.importPackage("com.test.arthas.instrument.collector");
                cp.importPackage("com.test.arthas.instrument.advice");
                cp.insertClassPath(new ByteArrayClassPath(name, classFileBuffer));
                CtClass cc = cp.get(name);
                cc.defrost();
                for (CtMethod declaredMethod : cc.getDeclaredMethods()) {
                    if (Modifier.isStatic(declaredMethod.getModifiers())) {
                        // 静态方法，跳过
                        continue;
                    }
                    declaredMethod.insertAfter(
                            String.format("WatchCollector.add(\"%s\", new Advice(this.getClass().getClassLoader(), this.getClass(), this, $args, ($w)$_, Boolean.FALSE));", sessionId));
                    CtClass exceptionCtClass = cp.get("java.lang.Exception");
                    declaredMethod.addCatch(
                            String.format("{WatchCollector.add(\"%s\", new Advice(this.getClass().getClassLoader(), this.getClass(), this, $args, null, Boolean.TRUE));throw $e;}", sessionId),
                            exceptionCtClass);
                }
                return cc.toBytecode();
            } else {
                return originalBytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classFileBuffer;
    }

    public void setWatch(boolean watch) {
        this.watch = watch;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setWatchClassName(String watchClassName) {
        this.watchClassName = watchClassName;
    }

    public String getWatchClassName() {
        return watchClassName;
    }

}
