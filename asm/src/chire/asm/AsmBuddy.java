package chire.asm;

import chire.asm.dynamic.builder.ClassBuilder;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy().defineClass("TestCl", Object.class)
                    .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
                        put("args", String[].class);
                    }})
                    ._return()
                    .make()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AsmBuddy() {
        this(new ClassAsm());
    }

    public AsmBuddy(ClassAsm classAsm) {
        this.classAsm = classAsm;
    }

    public ClassBuilder defineClass(String name, Class<?> superClass) {
        classAsm.defineClass(name, superClass);

        return new ClassBuilder(classAsm);
    }
}
