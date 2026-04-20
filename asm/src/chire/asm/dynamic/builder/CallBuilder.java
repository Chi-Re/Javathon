package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class CallBuilder<T> extends Builder<T>{
    public CallBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public static class ParameterBuilder<T> extends Builder<T>{
        public final Object[] parameters;

        public ParameterBuilder(ClassAsm classAsm, Class<T> type, Object[] parameters) {
            super(classAsm, type);
            this.parameters = parameters;
        }

        public CallBuilder<T> definitObj(Object... obj) {
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

            for (int i = 0; i < argSum; i++) {
                classAsm.ldcInsn(obj[i]);
            }

            if (clazzPack != null) classAsm.ldcInsns(clazzPack, Arrays.copyOfRange(obj, argSum, obj.length));

            return new CallBuilder<>(classAsm, type);
        }
    }

    public static class MethodBuilder<T> extends Builder<T>{
        AsmBudVisitor.AsmCallBuilder<T> callBuilder;

        public final Object[] parameters;

        public MethodBuilder(ClassAsm classAsm, Class<T> type, Object[] parameters) {
            super(classAsm, type);
            this.parameters = parameters;
        }

        public CallBuilder<T> setContent(AsmBudVisitor<T> builder) {
            return callBuilder.visit(builder.visit(new ParameterBuilder<>(classAsm, type, this.parameters)));
        }

        private void end(AsmBudVisitor.AsmCallBuilder<T> builder){
            callBuilder = builder;
        }
    }

    public MethodBuilder<T> callMethod(Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        return callMethod(Opcodes.INVOKEVIRTUAL, owner, var, parameters, returnType);
    }

    public MethodBuilder<T> callMethod(int opcode, Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        MethodBuilder<T> methodBuilder = new MethodBuilder<>(classAsm, type, parameters);

        methodBuilder.end(builder -> {
            classAsm.invokeMethod(opcode, owner, var, Format.formatParameter(parameters, returnType));

            if (returnType != null) classAsm.mVisitInsn(Opcodes.POP);

            return new CallBuilder<>(classAsm, type);
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

            if (returnType != null) classAsm.mVisitInsn(Opcodes.POP);

            return new CallBuilder<>(classAsm, type);
        });

        return methodBuilder;
    }


    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        classAsm.invokeVar(opcode, owner, var, Format.formatPack(type));

        return this;
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, String type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return this;
    }

    public CallBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new CallBuilder<>(classAsm, type);
    }

    public T out(){
        return this.create();
    }
}
