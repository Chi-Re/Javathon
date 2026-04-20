package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Types;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy("TestCl", Object.class).create()
                    .declareVar(Opcodes.ACC_PUBLIC, "a", Object.class)
                        .setContent(builder -> {
                            return builder.call(Opcodes.GETSTATIC, System.class, "out", "Ljava/io/PrintStream");
                        })
                    .declareStaticVar("st", String.class)
                        .setContent(builder -> builder.definitObj("setStaticVar"))
                    .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
                        put("args", String[].class);
                    }})
                        .call(Opcodes.GETSTATIC, System.class, "out", "Ljava/io/PrintStream")
                        .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/String",}, null)
                        .setContent(builder -> builder.definitObj("aaaaaaaa"))
                        .out()

//                        .callMethod("chire/python/lib/JPUtil", "newInstance", new String[]{"Ljava/lang/Class", "[Ljava/lang/Object"}, "Ljava/lang/Object")
//                        .setContent(builder -> {
//                            return builder.definitObj(Type.getType("Ljava/lang/String;"), "sss");
//                        })
//                        .out()

                        .call(Opcodes.GETSTATIC, System.class, "out", "Ljava/io/PrintStream")
                        .callMethod("java/io/PrintStream", "printf", new String[]{"Ljava/lang/String", "[Ljava/lang/Object"}, "Ljava/io/PrintStream")
                        .setContent(builder -> builder.definitObj("%.2f", 12.456, 14.344))
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

    public ClassAsm getClassAsm() {
        return classAsm;
    }

    public byte[] save() {
        return classAsm.getByte();
    }
}
