package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.util.Format;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class BlockBuilder<T extends BlockBuilder<T>> extends Builder<T> {
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

    public class IfElseBuilder extends Builder<T> {
        Label label = new Label();
        Label exit = new Label();

        public IfElseBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private IfElseBuilder setIn(Label in) {
            exit = in;
            return this;
        }

        public IfElseBuilder setContent(
                AsmBudVisitor.AsmBlockBuilder<T> condition,
                AsmBudVisitor.IfBuilder<T> visitor,
                int opcode
        ) {
            BlockBuilder<T> callBuilder = condition.visit(new BlockBuilder<>(classAsm, type));

            callBuilder.classAsm.jumpInsn(opcode, label);

            BlockBuilder<T> ke = visitor.visit(callBuilder);

            ke.classAsm.jumpInsn(Opcodes.GOTO, exit);
            ke.classAsm.mLabel(label);
            return new IfElseBuilder(ke.classAsm, ke.type).setIn(exit);
        }

        public BlockBuilder<T> toElse(AsmBudVisitor.IfBuilder<T> visitor) {
            BlockBuilder<T> builder = visitor.visit(create());

            builder.classAsm.mLabel(exit);

            return builder;
        }

        public BlockBuilder<T> out() {
            classAsm.mLabel(exit);
            return new BlockBuilder<>(classAsm, type);
        }
    }

    public IfElseBuilder ifCall() {
        return new IfElseBuilder(classAsm, type);
    }

    public CallBuilder<T> callLocal(String name) {
        return new CallBuilder<>(classAsm, type).callLocal(name);
    }

    public CallBuilder.MethodBuilder<T> callClass(Class<?> owner, Class<?>[] parameters) {
        return new CallBuilder<>(classAsm, this.type).callClass(owner, parameters);
    }

    public CallBuilder<T> callStatic(String var, Class<?> type) {
        return new CallBuilder<>(classAsm, this.type).callStatic(var, type);
    }

    public CallBuilder<T> callStatic(String var, String type) {
        return new CallBuilder<>(classAsm, this.type).callStatic(var, type);
    }
    public CallBuilder.MethodBuilder<T> callMethod(Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        return new CallBuilder<>(classAsm, this.type).callMethod(Opcodes.INVOKESTATIC, owner, var, parameters, returnType);
    }

    public CallBuilder.MethodBuilder<T> callMethod(String owner, String var, String[] parameters, String returnType) {
        return new CallBuilder<>(classAsm, this.type).callMethod(Opcodes.INVOKESTATIC, owner, var, parameters, returnType);
    }

    public CallBuilder<T> call(int opcode, String owner, String var, String type) {
        return new CallBuilder<>(classAsm,  this.type).call(opcode, owner, var, type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        return new CallBuilder<>(classAsm,  this.type).call(opcode, owner, var, type);
    }

    public T out() {
        return create();
    }
}
