package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.ConstructDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;

public class CallBuilder<T extends BlockBuilder<T>> extends Builder<T>{
    public CallBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public static class ParameterBuilder<T extends BlockBuilder<T>> extends Builder<T>{
        public final Object[] parameters;

        public ParameterBuilder(ClassAsm classAsm, Class<T> type, Object[] parameters) {
            super(classAsm, type);
            this.parameters = parameters;
        }

        public CallBuilder<T> callLocal(String... names) {
            CallBuilder<T> callBuilder = new CallBuilder<>(this.classAsm, this.type);

            for (String name : names) {
                callBuilder = callBuilder.callLocal(name);
            }

            return callBuilder;
        }

        public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
            return new CallBuilder<>(this.classAsm, this.type).call(opcode, owner, var, type);
        }

        public CallBuilder<T> call(int opcode, String owner, String var, String type) {
            return new CallBuilder<>(this.classAsm, this.type).call(opcode, owner, var, type);
        }

        public CallBuilder<T> callStatic(String var, Class<?> type) {
            return new CallBuilder<>(this.classAsm, this.type).callStatic(var, type);
        }

        public CallBuilder<T> callStatic(String var, String type) {
            return new CallBuilder<>(this.classAsm, this.type).callStatic(var, type);
        }

        public CallBuilder<T> definitObj(Object... objs){
            AsmBudVisitor.AsmCallBuilder<T>[] callBuilders = new AsmBudVisitor.AsmCallBuilder[objs.length];

            for (int i = 0; i < objs.length; i++) {
                int finalI = i;
                callBuilders[i] = new AsmBudVisitor.AsmCallBuilder<T>() {
                    @Override
                    public CallBuilder<T> visit(CallBuilder<T> builder) {
                        builder.classAsm.ldcInsn(objs[finalI]);
                        return builder;
                    }
                };
            }

            return definitPar(callBuilders);
        }

        public CallBuilder<T> definitPar(AsmBudVisitor.AsmCallBuilder... obj) {
            int argSum = 0;

            String clazzPack = null;

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof Class<?>) {
                    if (!((Class<?>)parameters[i]).isArray()) {
                        argSum ++;
                        continue;
                    }

                    if (i != parameters.length - 1) throw new RuntimeException("错误: varargs 参数必须是最后一个参数");

                    clazzPack = Format.formatStrType(((Class<?>)parameters[i]));
                } else if (parameters[i] instanceof String){
                    if (!((String)parameters[i]).startsWith("[")) {
                        argSum ++;
                        continue;
                    }

                    if (i != parameters.length - 1) throw new RuntimeException("错误: varargs 参数必须是最后一个参数");

                    clazzPack = Format.formatStrType(((String)parameters[i]));
                } else {
                    throw new RuntimeException("no key");
                }
            }

            CallBuilder<T> callBuilder = new CallBuilder<>(classAsm, type);

            for (int i = 0; i < argSum; i++) {
                if (i >= obj.length) break;
                callBuilder = obj[i].visit(callBuilder);
            }

            if (clazzPack != null) callBuilder = toVarargs(clazzPack, callBuilder, Arrays.copyOfRange(obj, argSum, obj.length));

            return callBuilder;
        }

        private CallBuilder<T> toVarargs(String clazzPack, CallBuilder<T> callBuilder, AsmBudVisitor.AsmCallBuilder<T>... objs) {
            int ICONST_NUM = Opcodes.ICONST_0 + objs.length;

            callBuilder.classAsm.mVisitInsn(ICONST_NUM);
            callBuilder.classAsm.mVisitTypeInsn(ANEWARRAY, clazzPack);

            for (int i = ICONST_0; i < ICONST_NUM; i++) {
                callBuilder.classAsm.mVisitInsn(DUP);
                callBuilder.classAsm.mVisitInsn(i);

                callBuilder = objs[i-ICONST_0].visit(callBuilder);

                callBuilder.classAsm.mVisitInsn(AASTORE);
            }

            return callBuilder;
        }
    }

    public MethodBuilder<T> callClass(Class<?> owner, Class<?>[] parameters) {
        MethodBuilder<T> methodBuilder = new MethodBuilder<>(classAsm, type, parameters);

        classAsm.newClass(owner);

        methodBuilder.end(builder -> {
            classAsm.invokeMethod(Opcodes.INVOKESPECIAL, owner, "<init>", Format.formatParameter(parameters, null));
//            classAsm.mVisitInsn(Opcodes.DUP); //TODO 理应使用，但并没有，使用反而不能运行。

            return this;
        });

        return methodBuilder;
    }

    public static class MethodBuilder<T extends BlockBuilder<T>> extends Builder<T>{
        AsmBudVisitor.AsmCallBuilder<T> callBuilder;

        public final Object[] parameters;

        public MethodBuilder(ClassAsm classAsm, Class<T> type, Object[] parameters) {
            super(classAsm, type);
            this.parameters = parameters;
        }

        public CallBuilder<T> setContent() {
            return setContent(builder -> new CallBuilder<>(builder.classAsm, builder.type));
        }

        public CallBuilder<T> setContent(AsmBudVisitor<T> builder) {
            classAsm.setState("set-content-method");
            return callBuilder.visit(builder.visit(new ParameterBuilder<>(classAsm, type, this.parameters)));
        }

        private void end(AsmBudVisitor.AsmCallBuilder<T> builder){
            callBuilder = builder;
        }

        public T breakCall() {
            return create();
        }
    }

    public MethodBuilder<T> callMethod(Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        return callMethod(Opcodes.INVOKEVIRTUAL, owner, var, parameters, returnType);
    }

    public MethodBuilder<T> callMethod(int opcode, Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        MethodBuilder<T> methodBuilder = new MethodBuilder<>(classAsm, type, parameters);

        methodBuilder.end(builder -> {
            classAsm.releaseState();
            classAsm.invokeMethod(opcode, owner, var, Format.formatParameter(parameters, returnType));

            if (returnType != null && !classAsm.getState().contains("content")) {
                classAsm.mVisitInsn(Opcodes.POP);
            }
//            if (returnType != null) classAsm.mVisitInsn(Opcodes.POP);
            return this;
        });

        return methodBuilder;
    }

    public MethodBuilder<T> callMethod(String owner, String var, String[] parameters, String returnType) {
        return callMethod(Opcodes.INVOKEVIRTUAL, owner, var, parameters, returnType);
    }

    public MethodBuilder<T> callMethod(int opcode, String owner, String var, String[] parameters, String returnType) {
        MethodBuilder<T> methodBuilder = new MethodBuilder<>(classAsm, type, parameters);

        methodBuilder.end(builder -> {
            classAsm.invokeMethod(opcode, owner, var, Format.formatStrParameter(parameters, returnType));

            if (returnType != null && !classAsm.getState().contains("content")) classAsm.mVisitInsn(Opcodes.POP);
//            if (returnType != null) classAsm.mVisitInsn(Opcodes.POP);

            return this;
        });

        return methodBuilder;
    }

    public CallBuilder<T> callLocal(String name) {
        classAsm.invokeLocalVar(name);

        return this;
    }

    public CallBuilder<T> callThis(){
        classAsm.invokeThis();

        return this;
    }

    public CallBuilder<T> call(Class<?> owner, String var, Class<?> type) {
        return call(Opcodes.GETSTATIC, owner, var, type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return this;
    }

    public CallBuilder<T> call(String owner, String var, String type) {
        return call(Opcodes.GETSTATIC, owner, var, type);
    }

    public CallBuilder<T> call(int opcode, String owner, String var, String type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return this;
    }

    public CallBuilder<T> callClassVar(String var, String type) {
        classAsm.invokeStaticVar(GETFIELD, var, type);

        return this;
    }

    public CallBuilder<T> callClassVar(String var, Class<?> type) {
        CallBuilder<T> callBuilder = callThis();
        callBuilder.classAsm.invokeStaticVar(GETFIELD, var, type);

        return callBuilder;
    }

    public CallBuilder<T> callStatic(String var, Class<?> type) {
        classAsm.invokeStaticVar(Opcodes.GETSTATIC, var, type);

        return this;
    }

    public CallBuilder<T> callStatic(Class<?> owner, String var, Class<?> type) {
        classAsm.invokeStaticVar(Opcodes.GETSTATIC, owner, var, type);

        return this;
    }

    public CallBuilder<T> callStatic(String var, String type) {
        classAsm.invokeStaticVar(Opcodes.GETSTATIC, var, type);

        return this;
    }

    public CallBuilder<T> callStatic(String owner, String var, String type) {
        classAsm.invokeStaticVar(Opcodes.GETSTATIC, owner, var, type);

        return this;
    }

    public CallBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return this;
    }

//    public static class SetBuilder<T extends BlockBuilder<T>> extends Builder<T> {
//        private AsmBudVisitor.SetBlockBuilder<T> par;
//
//        public SetBuilder(ClassAsm classAsm, Class<T> type) {
//            super(classAsm, type);
//        }
//
//        public CallBuilder<T> setContent(AsmBudVisitor.AsmBlockBuilder<T> content) {
//            return content.visit()
//        }
//
//        public void setPar(AsmBudVisitor.SetBlockBuilder<T> par) {
//            this.par = par;
//        }
//    }
//
//    public SetBuilder<T> definiPar(AsmBudVisitor.SetBlockBuilder<T> var) {
//        SetBuilder<T> setBuilder = new SetBuilder<>(classAsm, type);
//
//        setBuilder.setPar(var);
//
//        return setBuilder;
//    }

    public BlockBuilder.VarBuilder<T> setVar(String name) {
        return new BlockBuilder<>(classAsm, this.type).setVar(name);
    }

    public BlockBuilder.ClassVarBuilder<T> setClassVar(int opcode, String name, Class<?> type) {
        if (this.type.equals(ClinitDefinition.class)) throw new RuntimeException("no key");
        return new BlockBuilder<>(classAsm, this.type).setClassVar(opcode, name, type);
    }

    public BlockBuilder.ClassVarBuilder<T> setClassVar(int opcode, String name, String type) {
        if (this.type.equals(ClinitDefinition.class)) throw new RuntimeException("no key");
        return new BlockBuilder<>(classAsm, this.type).setClassVar(opcode, name, type);
    }

    public BlockBuilder.ClassVarBuilder<T> setStaticVar(String name, String type) {
        return new BlockBuilder<>(classAsm, this.type).setStaticVar(name, type);
    }

    public BlockBuilder.ClassVarBuilder<T> setStaticVar(String name, Class<?> type) {
        return new BlockBuilder<>(classAsm, this.type).setStaticVar(name, type);
    }

    public BlockBuilder.ClassVarBuilder<T> setStaticVar(String owner, String name, String type) {
        return new BlockBuilder<>(classAsm, this.type).setStaticVar(owner, name, type);
    }

    public BlockBuilder.ClassVarBuilder<T> setStaticVar(Class<?> owner, String name, Class<?> type) {
        return new BlockBuilder<>(classAsm, this.type).setStaticVar(owner, name, type);
    }

    public T _break(){
        return this.create();
    }
}
