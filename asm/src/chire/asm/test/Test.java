package chire.asm.test;

public class Test {
    public Object a;
    public static String st = "setStaticVar";

    public static void main(String[] var0) {
        System.out.println(new Test());
    }

    private void $__init__$FieldInsn$() {
        this.a = System.out;
    }

    public Test() {
        this.$__init__$FieldInsn$();
    }

    //            methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    //            methodVisitor.visitFieldInsn(GETSTATIC, "TestCl", "st", "Ljava/lang/String;");
    //            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
    //            methodVisitor.visitLdcInsn("setStaticVar");
    //            methodVisitor.visitFieldInsn(PUTSTATIC, "TestCl", "st", "Ljava/lang/String;");
    //            methodVisitor.visitInsn(RETURN);
    //            methodVisitor.visitMaxs(2, 0);
    //            methodVisitor.visitEnd();

    //            methodVisitor.visitLdcInsn(Type.getType("Ljava/lang/System;"));
    //            methodVisitor.visitFieldInsn(PUTSTATIC, "ClassPyTest", "JPClass_System", "Ljava/lang/Class;");
    //            methodVisitor.visitIntInsn(SIPUSH, 1);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    //            methodVisitor.visitFieldInsn(PUTSTATIC, "ClassPyTest", "a", "Ljava/lang/Object;");
    //            methodVisitor.visitIntInsn(SIPUSH, 3);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    //            methodVisitor.visitFieldInsn(PUTSTATIC, "ClassPyTest", "b", "Ljava/lang/Object;");
    //            methodVisitor.visitLdcInsn("println");
    //            methodVisitor.visitInsn(ICONST_1);
    //            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    //            methodVisitor.visitInsn(DUP);
    //            methodVisitor.visitInsn(ICONST_0);
    //            methodVisitor.visitLdcInsn("test");
    //            methodVisitor.visitInsn(AASTORE);
    //            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "chire/python/escape/ClassCall", "callMethod", "(Ljava/lang/String;[Ljava/lang/Object;)Lchire/python/escape/ClassCall;", false);
    //            methodVisitor.visitInsn(RETURN);
    //            methodVisitor.visitMaxs(5, 0);
    //            methodVisitor.visitEnd();
    //        }
}


