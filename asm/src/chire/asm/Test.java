package chire.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class Test {
    public String a;

    public static void main(String[] var0) {
        System.out.println("aaa");
    }

    private void $__init__$FieldInsn$() {
        this.a = "ssssss";
    }

    public Test() {
        this.$__init__$FieldInsn$();
    }
}

//        {
//            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "$__init__$FieldInsn$", "()V", null, null);
//            methodVisitor.visitCode();
//            methodVisitor.visitVarInsn(ALOAD, 0);
//            methodVisitor.visitLdcInsn("ssssss");
//            methodVisitor.visitFieldInsn(PUTFIELD, "chire/asm/Test", "a", "Ljava/lang/String;");
//            methodVisitor.visitInsn(RETURN);
//            methodVisitor.visitMaxs(2, 1);
//            methodVisitor.visitEnd();
//        }


//        {
//            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "$__init__$FieldInsn$", "()V", null, null);
//            methodVisitor.visitCode();
//            methodVisitor.visitVarInsn(ALOAD, 0);
//            methodVisitor.visitLdcInsn("ssssss");
//            methodVisitor.visitFieldInsn(PUTFIELD, "TestCl", "a", "Ljava/lang/String;");
//            methodVisitor.visitInsn(RETURN);
//            methodVisitor.visitMaxs(0, 1);
//            methodVisitor.visitEnd();
//        }
