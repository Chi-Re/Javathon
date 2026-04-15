package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

public class BlockBuilder<T> extends Builder<T> {
    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public DefinitBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new DefinitBuilder<>(classAsm, type);
    }

    public static class VarBuilder<T> extends Builder<T> {
        private String name;
        public VarBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private void setVar(String name) {
            this.name = name;
        }

        public T setContent(AsmBudVisitor.AsmCallBuilder<T> visitor) {
            CallBuilder<T> builder = visitor.visit(new CallBuilder<>(classAsm, type));

            builder.classAsm.varInsn(name);
            return builder.create();
        }
    }

    public static class ClassVarBuilder<T> extends Builder<T> {
        private int opcode;
        private String name;
        private String varType;

        public ClassVarBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private void setVar(int opcode, String name, String type) {
            this.opcode = opcode;
            this.name = name;
            this.varType = type;
        }

        private void setVar(int opcode, String name, Class<?> type) {
            setVar(opcode, name, Format.formatPack(type));
        }

        public T setContent(AsmBudVisitor.AsmCallBuilder<T> visitor) {
            CallBuilder<T> builder = visitor.visit(new CallBuilder<>(classAsm, type));

            builder.classAsm.invokeClassVarEnd(opcode, name, varType);
            return builder.create();
        }
    }

    public VarBuilder<T> setVar(String name) {
        VarBuilder<T> varBuilder = new VarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(name);
        return varBuilder;
    }

    public ClassVarBuilder<T> setClassVar(int opcode, String name, String type) {
        classAsm.thisInsn();

        ClassVarBuilder<T> varBuilder = new ClassVarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(opcode, name, type);

        return varBuilder;
    }

    public ClassVarBuilder<T> setClassVar(int opcode, String name, Class<?> type) {
        return setClassVar(opcode, name, Format.formatPack(type));
    }

    public ClassVarBuilder<T> setStaticVar(String name, String type) {
        ClassVarBuilder<T> varBuilder = new ClassVarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(Opcodes.PUTSTATIC, name, type);

        return varBuilder;
    }

    public ClassVarBuilder<T> setStaticVar(String name, Class<?> type) {
        return setStaticVar(name, Format.formatPack(type));
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        return call(opcode, owner, var, Format.formatPack(type));
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, String type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return new CallBuilder<>(classAsm,  this.type);
    }
}
