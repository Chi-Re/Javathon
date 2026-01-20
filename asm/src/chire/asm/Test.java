package chire.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class Test {
    public String a;

    public static void main(String[] var0) {
        System.out.println("aaa");
        System.out.println(new Test2().c);
    }

    private void $__init__$FieldInsn$() {
        this.a = "ssssss";
    }

    public Test() {
        this.$__init__$FieldInsn$();
    }

    public static class Test2{
        public String c = "lll";

        public Test2() {}
    }
}

