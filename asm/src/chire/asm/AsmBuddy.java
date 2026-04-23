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
import java.util.List;
import java.util.Map;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy("TestCl", Object.class).create()
                    .declareVar(Opcodes.ACC_PUBLIC, "a", Object.class)
                    .setContent(builder -> {
                        return builder.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class);
                    })

                    .setContent(cliBuil -> {
                        return cliBuil.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                                .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                                .setContent(builder -> builder.callStatic("st", String.class));
                    })

                    .declareStaticVar("st", String.class)
                    .setContent(builder -> builder.definitObj("setStaticVar"))
                    .defineFunction(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", new Args(){{
                        put("args", String[].class);
                    }})

                    .setVar("c")
                    .setContent(funBuil ->
                            funBuil.definitObj(4)
                    )
                    .setVar("d")
                    .setContent(funBuil ->
                            funBuil.definitObj(3)
                    )

                    .ifCall()
                    .setContent(
                            condition -> {
                                return condition.callLocal("c").callMethod(Integer.class, "intValue", new Class[]{}, int.class).setContent()
                                        .callLocal("d").callMethod(Integer.class, "intValue", new Class[]{}, int.class).setContent();
                            },
                            ifBuil -> ifBuil
                                    .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                                    .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                                    .setContent(builder -> builder.callStatic("st", String.class))
                                    .out(),
                            Opcodes.IF_ICMPLE
                    )
                    .setContent(condition -> {
                                return condition.callLocal("c").callMethod(Integer.class, "intValue", new Class[]{}, int.class).setContent()
                                        .callLocal("d").callMethod(Integer.class, "intValue", new Class[]{}, int.class).setContent();
                            },
                            ifBuil -> ifBuil
                            .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                            .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                            .setContent(builder -> builder.callStatic("st", String.class))
                            .out(),
                            Opcodes.IF_ICMPNE
                    )
                    .toElse(elseto -> elseto.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                            .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                            .setContent(builder -> builder.callLocal("d"))
                            .out()
                    )

                    .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                    .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
                    .setContent(builder -> builder.callLocal("c"))
                    .out()

                    .call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
                    .callMethod("java/io/PrintStream", "printf", new String[]{"Ljava/lang/String", "[Ljava/lang/Object"}, "Ljava/io/PrintStream")
                    .setContent(builder -> builder.definitPar(
                            parBui -> parBui.callStatic("st", String.class)
                    ))
                    .out()
                    ._return()

                    .defineClass("OtherTestCl", Object.class)
                    .declareStaticVar("st", String.class)
                    .setContent(builder -> builder.definitObj("setStaticVar"))
                    .make()

                    .save().get("TestCl")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public Map<String, byte[]> save() {
        return classAsm.getByte();
    }
}
