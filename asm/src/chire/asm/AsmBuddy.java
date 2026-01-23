package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.FunctionDefinition;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy("TestCl", Object.class).create()
                    .defineVar("a", String.class, "ssssss")
                    .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
                        put("args", String[].class);
                    }})
                            .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                            .callMethod(PrintStream.class, "println", new Class[]{String.class,}, null)
                            这里模拟存在的评论之类的，也可以在这里写注释
                            .out()
                    ._return()
                    .make()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AsmBuddy(String name, Class<?> superClass) {
        this(new ClassAsm(name, superClass));
    }

    public AsmBuddy(ClassAsm classAsm) {
        this.classAsm = classAsm;
    }

    public ClassBuilder create() {
        return new ClassBuilder(classAsm);
    }
}
