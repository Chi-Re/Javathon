package chire.asm.test;

public class Test {
    public static void main(String[] param0) {
        Boolean c = true;

        if (c) {
            System.out.print("ssssss");
        }

        //methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
    }

    static {
    }

    public static boolean comparison(Integer k, Integer p) {
        return k > p;
    }
}


