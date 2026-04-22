package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Types;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

                        .setVar("c")
                        .setContent(builder -> builder.callClass(String.class, new Class[]{String.class}).setContent(
                                builder1 -> builder1.definitObj("ddddd")
                        ))

                        .callClass(String.class, new Class[]{String.class}).setContent(
                                builder1 -> builder1.definitObj("sssss")
                        )

                        .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                        .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                        .setContent(builder -> builder.callStatic("st", String.class))
                        .out()

                        .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                        .callMethod("java/io/PrintStream", "printf", new String[]{"Ljava/lang/String", "[Ljava/lang/Object"}, "Ljava/io/PrintStream")
                        .setContent(builder -> builder.definitPar(
                                parBui -> parBui.callStatic("st", String.class),
                                parBui -> parBui.callStatic("st", String.class),
                                parBui -> parBui.callStatic("st", String.class)
                        ))
                        .out()
                    ._return()
                    .make()
                    .save()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        ByteArrayClassLoader loader = new ByteArrayClassLoader();
//
//        Class<?> clazz = loader.defineClass("chire.test.TestCl", new AsmBuddy("chire.test.TestCl", Object.class).create()
//                .declareVar(Opcodes.ACC_PUBLIC, "a", Object.class)
//                .setContent(builder -> {
//                    return builder.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class);
//                })
//                .declareStaticVar("st", String.class)
//                .setContent(builder -> builder.definitObj("setStaticVar"))
//                .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
//                    put("args", String[].class);
//                }})
//
//                .setVar("c")
//                .setContent(builder -> builder.callClass(String.class, new Class[]{String.class}).setContent(
//                        builder1 -> builder1.definitObj("ddddd")
//                ))
//
//                .callClass(String.class, new Class[]{String.class}).setContent(
//                        builder1 -> builder1.definitObj("sssss")
//                )
//
//                .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
//                .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
//                .setContent(builder -> builder.callStatic("st", String.class))
//                .out()
//
//                .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
//                .callMethod("java/io/PrintStream", "printf", new String[]{"Ljava/lang/String", "[Ljava/lang/Object"}, "Ljava/io/PrintStream")
//                .setContent(builder -> builder.definitObj("%.2f", 12.456, 14.344))
//                .out()
//                ._return()
//                .make()
//                .save());
//
//        System.out.println(clazz.getMethod("main", String[].class).invoke(null, (Object) new String[0]));
    }

    static class ByteArrayClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
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
