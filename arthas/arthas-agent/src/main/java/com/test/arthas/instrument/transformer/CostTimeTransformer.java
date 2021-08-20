package com.test.arthas.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

public class CostTimeTransformer implements ClassFileTransformer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CostTimeTransformer.class);
    private static final Set<String> OBJ_METHODS;

    static {
        OBJ_METHODS = new HashSet<>();
        for (Method method : Object.class.getMethods()) {
            OBJ_METHODS.add(method.getName());
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        try {
            if (className == null
                    || className.contains("$$")
                    || className.startsWith("java")) {
                return classFileBuffer;
            }

            String name = className.replace('/', '.');

            if (!name.startsWith("com.test")) {
                return classFileBuffer;
            }
            ClassPool cp = ClassPool.getDefault();
            cp.importPackage("com.test.arthas.instrument.collector");
            cp.insertClassPath(new ByteArrayClassPath(name, classFileBuffer));
            CtClass cc = cp.get(name);

            if (cc.isFrozen()) {
                cc.defrost();
            }
            for (CtMethod ctMethod : cc.getDeclaredMethods()) {
                String methodName = ctMethod.getName();
                if (methodName.contains("$")) {
                    continue;
                }
                if (OBJ_METHODS.contains(methodName)) {
                    continue;
                }
                if (Modifier.isNative(ctMethod.getModifiers())) {
                    continue;
                }

                if (ctMethod.isEmpty()) {
                    continue;
                }
                ctMethod.addLocalVariable("startCtTime", CtClass.longType);
                ctMethod.insertBefore("startCtTime=System.currentTimeMillis();");
                ctMethod.insertAfter("CostTimeCollector.addCostTime(Thread.currentThread(), Thread.currentThread().getStackTrace(), System.currentTimeMillis() - startCtTime);");
            }
            LOGGER.info("class:{} methods add cost times aspect", className);
            return cc.toBytecode();
        } catch (Exception e) {
            LOGGER.error("transform class({}) exception", className, e);
        }
        return classFileBuffer;
    }
}
