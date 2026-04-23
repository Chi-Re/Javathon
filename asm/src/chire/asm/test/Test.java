package chire.asm.test;

public class Test {
    public static Class System;


    private void $__init__$FieldInsn$() {
    }

    static {
        main();
    }

    public static void main(Object... ms) {

    }

    public Test() {
        this.$__init__$FieldInsn$();
    }

    //        {
    //            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    //            methodVisitor.visitCode();
    //            Label label0 = new Label();
    //            methodVisitor.visitLabel(label0);
    //            methodVisitor.visitLineNumber(11, label0);
    //            methodVisitor.visitInsn(ICONST_0);
    //            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "chire/asm/test/Test", "main", "([Ljava/lang/Object;)V", false);
    //            Label label1 = new Label();
    //            methodVisitor.visitLabel(label1);
    //            methodVisitor.visitLineNumber(12, label1);
    //            methodVisitor.visitInsn(RETURN);
    //            methodVisitor.visitMaxs(1, 0);
    //            methodVisitor.visitEnd();
    //        }

    //        {
    //            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
    //            methodVisitor.visitCode();
    //            methodVisitor.visitLdcInsn(Type.getType("Ljava/lang/System;"));
    //            methodVisitor.visitFieldInsn(PUTSTATIC, "chire/test/py/ClassPyTest", "System", "Ljava/lang/Class;");
    //            methodVisitor.visitTypeInsn(NEW, "chire/python/escape/ClassCall");
    //            methodVisitor.visitInsn(DUP);
    //            methodVisitor.visitLdcInsn(Type.getType("Lchire/test/py/ClassPyTest;"));
    //            methodVisitor.visitMethodInsn(INVOKESPECIAL, "chire/python/escape/ClassCall", "<init>", "(Ljava/lang/Object;)V", false);
    //            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "chire/python/escape/ClassCall", "callMethod", "(Ljava/lang/String;[Ljava/lang/Object;)Lchire/python/escape/ClassCall;", false);
    //            methodVisitor.visitInsn(RETURN);
    //            methodVisitor.visitMaxs(3, 0);
    //            methodVisitor.visitEnd();
    //        }
}


