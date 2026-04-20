package chire.asm.test;

import org.objectweb.asm.Opcodes;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public Object a;
    public static String st = "setStaticVar";

    public static void main(String[] var0) {
        String text = "sssssssdaaa";

        System.out.println(text);
    }

    private void $__init__$FieldInsn$() {
        this.a = System.out;
    }

    public Test() {
        this.$__init__$FieldInsn$();
    }
}


