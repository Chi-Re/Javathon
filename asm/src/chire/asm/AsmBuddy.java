package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.ClassBuilder;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class AsmBuddy {
    private ClassAsm classAsm;

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        try (FileOutputStream fos = new FileOutputStream("cache/test/TestCl.class")) {
            fos.write(new AsmBuddy("TestCl", Object.class).create()
                    .declareStaticVar("System", Class.class)
                    .setContent(builder -> builder.definitObj(Type.getType("Ljava/lang/System;")))
                    .setContent(builder -> {
                        return builder.ifCall()
                                .setContent(
                                        pd -> {
                                            return pd.callMethod("chire/python/escape/JPUtil", "comparison", new String[]{"Ljava/lang/Integer", "Ljava/lang/Integer", "Ljava/lang/String"}, Type.getType(Boolean.class).toString())
                                                    .setContent(qd -> qd.definitObj(1, 2, "sss"))._break();
                                        },
                                        ifBui -> {
//                                            return ifBui.call(Opcodes.GETSTATIC, System.class, "out", PrintStream.class)
//                                                    .callMethod("java/io/PrintStream", "println", new String[]{"Ljava/lang/Object",}, null)
//                                                    .setContent(inn -> inn.callStatic("st", String.class))
//                                                    ._break();

                                            return ifBui.callMethod(
                                                    "chire/python/escape/JPUtil",
                                                    "callVar",
                                                    new String[]{"Ljava/lang/Object", "Ljava/lang/String"},
                                                    "Ljava/lang/Object"
                                            ).setContent(bexbui -> {
                                                return bexbui.definitPar(
                                                        par -> par.callStatic("System", Class.class),
                                                        par -> par.definitObj("out")
                                                );
                                            })._break();
                                        },
                                        Opcodes.IFEQ
                                ).out().out();
                    })
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
        this(ClassAsm.create(new Class[]{String.class, Class.class}, new Object[]{name, superClass}));
//        this(new ClassAsm(name, superClass));
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
