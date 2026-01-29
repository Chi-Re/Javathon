package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy("TestCl", Object.class).create()
                    .declareVar(Opcodes.ACC_PUBLIC, "a", Object.class)
                        .setContent(builder -> {
                            return builder.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class);
                        })
                    .declareStaticVar("st", String.class)
                        .setContent(builder -> builder.definitObj("setStaticVar"))
                    .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
                        put("args", String[].class);
                    }})
                        .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                        .callMethod(PrintStream.class, "println", new Class[]{String.class,}, null)
                        .setContent(builder -> builder.definitObj("aaaaaaaa"))
                        .out()
                        .setVar("c")
                        .setContent(builder -> builder.definitObj(12))
                    ._return()
                    .make()
                    .save()
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

    public byte[] save() {
        return classAsm.getByte();
    }
}
