package com.test.asm.visitor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 修改类字段方法的可见性
 */
public class AccessChangeClassAdapter extends ClassVisitor {

    public AccessChangeClassAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        // 修改成员变量的可见性
        return super.visitField(Opcodes.ACC_PRIVATE, name, descriptor, signature, value);
    }

}
