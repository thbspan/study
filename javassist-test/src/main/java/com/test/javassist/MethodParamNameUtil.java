package com.test.javassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class MethodParamNameUtil {

    private MethodParamNameUtil() {}

    protected static String[] getMethodParamNames(CtMethod cm) {
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
                .getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            return new String[0];
        }
        String[] paramNames;
        try {
            paramNames = new String[cm.getParameterTypes().length];
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos);
        }
        return paramNames;
    }

    /**
     * 获取方法参数名称，按给定的参数类型匹配方法
     *
     * @param clazz      类名
     * @param method     方法名称
     * @param paramTypes 方法参数列表
     * @return 方法的参数名称数组
     */
    public static String[] getMethodParamNames(Class<?> clazz, String method, Class<?>... paramTypes) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(clazz.getName());

            String[] paramTypeNames = new String[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypeNames[i] = paramTypes[i].getName();
            }

            CtMethod cm = cc.getDeclaredMethod(method, pool.get(paramTypeNames));
            return getMethodParamNames(cm);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取方法参数名称，匹配同名的某一个方法
     *
     * @param clazz  类名
     * @param method 方法名称
     * @return 方法的参数名称数组
     */
    public static String[] getMethodParamNames(Class<?> clazz, String method) {
        ClassPool pool = ClassPool.getDefault();
        try {
            CtClass cc = pool.get(clazz.getName());
            CtMethod cm = cc.getDeclaredMethod(method);
            return getMethodParamNames(cm);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
