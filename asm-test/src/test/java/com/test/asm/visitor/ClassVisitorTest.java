package com.test.asm.visitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassVisitorTest {

    @Test
    public void test() throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        ClassVisitor classVisitor = new DelLoginMethodClassAdapter(Opcodes.ASM7, classWriter);

        classVisitor = new AccessChangeClassAdapter(Opcodes.ASM7, classVisitor);

        ClassReader classReader = new ClassReader(ClassVisitorTest.class.getResourceAsStream("../TestClass.class"));
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);

        byte[] data = classWriter.toByteArray();
        File file = new File("target/TestClass.class");
        try (FileOutputStream fout = new FileOutputStream(file)) {
            fout.write(data);
        }
    }
}
