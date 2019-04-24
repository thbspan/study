package com.test.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * 删除login方法的Adaptor
 */
public class DelLoginMethodClassAdapter extends ClassVisitor {
    public DelLoginMethodClassAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ("login".equals(name)) {
            // 删除某个方法，返回null
            return null;
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
