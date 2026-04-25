package chire.asm.test;

public class Test {
    public static void main(String[] param0) {
        System.exit((Integer) 1);
    }

    public static void tes() {
    }

    //            methodVisitor.visitInsn(ICONST_1);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    //            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(I)V", false);
    //            Label label1 = new Label();
    //            methodVisitor.visitLabel(label1);
    //            methodVisitor.visitLineNumber(6, label1);
    //            methodVisitor.visitInsn(RETURN);
    //            Label label2 = new Label();
    //            methodVisitor.visitLabel(label2);
    //            methodVisitor.visitLocalVariable("param0", "[Ljava/lang/String;", null, label0, label2, 0);
    //            methodVisitor.visitMaxs(1, 1);
    //            methodVisitor.visitEnd();
    //        }

    //        {
    //            methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
    //            methodVisitor.visitCode();
    //            methodVisitor.visitIntInsn(SIPUSH, 1);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
    //            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "exit", "(Ljava/lang/Integer;)V", false);
    //            methodVisitor.visitInsn(RETURN);
    //            methodVisitor.visitMaxs(1, 1);
    //            methodVisitor.visitEnd();
    //        }
}


