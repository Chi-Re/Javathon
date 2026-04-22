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
}


