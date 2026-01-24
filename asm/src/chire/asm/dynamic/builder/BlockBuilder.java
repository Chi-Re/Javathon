package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

public class BlockBuilder<T> extends Builder<T> {
    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public DefinitBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new DefinitBuilder<>(classAsm, type);
    }

    public static class VarBuilder<T> extends Builder<T> {
        private int opcode;
        private String name;
        private Class<?> varType;

        public VarBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private void setVar(int opcode, String name, Class<?> type) {
            this.opcode = opcode;
            this.name = name;
            this.varType = type;
        }

        public T setContent(AsmBudVisitor.AsmCallBuilder<T> visitor) {
            CallBuilder<T> builder = visitor.visit(new CallBuilder<>(classAsm, type));

            builder.classAsm.invokeClassVarEnd(opcode, name, varType);
            return builder.create();
        }
    }

    public VarBuilder<T> setVar(int opcode, String name, Class<?> type) {
        VarBuilder<T> varBuilder = new VarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(opcode, name, type);

        return varBuilder;
    }

    public VarBuilder<T> setClassVar(int opcode, String name, Class<?> type) {
        classAsm.thisInsn();
        return setVar(opcode, name, type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        classAsm.invokeVar(opcode, owner, var, Format.formatPack(type)+";");

        return new CallBuilder<>(classAsm,  this.type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, String type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return new CallBuilder<>(classAsm,  this.type);
    }
}
