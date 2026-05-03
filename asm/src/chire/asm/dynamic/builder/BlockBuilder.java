package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.GETFIELD;

public class BlockBuilder<T extends BlockBuilder<T>> extends Builder<T> {
    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public CallBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new CallBuilder<>(classAsm, type);
    }

    public static class VarBuilder<T extends BlockBuilder<T>> extends Builder<T> {
        private String name;
        public VarBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private void setVar(String name) {
            this.name = name;
        }

        public T setContent(AsmBudVisitor.SetCallBuilder<T> visitor) {
            classAsm.setState("set-content-var");
            CallBuilder<T> builder = visitor.visit(new BlockBuilder<>(classAsm, type));
            classAsm.releaseState();

            builder.classAsm.varInsn(name);
            return builder._break();
        }

        public T setBlockContent(AsmBudVisitor.SetBlockBuilder<T> visitor) {
            classAsm.setState("set-content-var");
            BlockBuilder<T> builder = visitor.visit(new BlockBuilder<>(classAsm, type));
            classAsm.releaseState();

            builder.classAsm.varInsn(name);
            return builder.out();
        }
    }

    public static class ClassVarBuilder<T extends BlockBuilder<T>> extends Builder<T> {
        private int opcode;
        private String name;
        private String varType;
        private String owner;

        public ClassVarBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private void setVar(int opcode, String name, String type) {
            setVar(opcode, null, name, type);
        }

        private void setVar(int opcode, String owner, String name, String type) {
            this.opcode = opcode;
            this.name = name;
            this.varType = type;
            this.owner = owner;
        }

        private void setVar(int opcode, String name, Class<?> type) {
            setVar(opcode, null, name, type);
        }

        private void setVar(int opcode, Class<?> owner, String name, Class<?> type) {
            setVar(opcode, owner == null ? null : Format.formatPack(owner, false), name, Format.formatPack(type));
        }

        public T setContent(AsmBudVisitor.AsmCallBuilder<T> visitor) {

            classAsm.setState("set-content-var");
            CallBuilder<T> builder = visitor.visit(new CallBuilder<>(classAsm, type));
            classAsm.releaseState();

            if (owner == null) {
                builder.classAsm.invokeClassVarEnd(opcode, name, varType);
            } else {
                builder.classAsm.invokeStaticVar(opcode, owner, name, varType);
            }
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

    public ClassVarBuilder<T> setStaticVar(String owner, String name, String type) {
        ClassVarBuilder<T> varBuilder = new ClassVarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(Opcodes.PUTSTATIC, owner, name, type);

        return varBuilder;
    }

    public ClassVarBuilder<T> setStaticVar(String name, Class<?> type) {
        ClassVarBuilder<T> varBuilder = new ClassVarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(Opcodes.PUTSTATIC, name, type);

        return varBuilder;
    }

    public ClassVarBuilder<T> setStaticVar(Class<?> owner, String name, Class<?> type) {
        ClassVarBuilder<T> varBuilder = new ClassVarBuilder<>(this.classAsm, this.type);
        varBuilder.setVar(Opcodes.PUTSTATIC, owner, name, type);

        return varBuilder;
    }

    public class IfElseBuilder extends Builder<T> {
        Label label = new Label();
        Label exit = new Label();

        AsmBudVisitor.IfBuilder<T> outBui;

        public IfElseBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        private IfElseBuilder setIn(AsmBudVisitor.IfBuilder<T> outBui) {
            this.outBui = outBui;
            return this;
        }

        public IfElseBuilder setContent(
                AsmBudVisitor.AsmBlockBuilder<T> condition,
                AsmBudVisitor.IfBuilder<T> visitor,
                int opcode
        ) {
            classAsm.setState("set-content-if");
            BlockBuilder<T> callBuilder = condition.visit(new BlockBuilder<>(classAsm, type));
            callBuilder.classAsm.releaseState();

            callBuilder.classAsm.jumpInsn(opcode, label);

            BlockBuilder<T> ke = visitor.visit(callBuilder);

            return new IfElseBuilder(ke.classAsm, ke.type).setIn(ouB -> {
                ouB.classAsm.mLabel(label);
                return ouB;
            });
        }

        public BlockBuilder<T> toElse(AsmBudVisitor.IfBuilder<T> visitor) {
            classAsm.jumpInsn(Opcodes.GOTO, exit);
            BlockBuilder<T> builder = this.outBui.visit(new BlockBuilder<>(classAsm, type));
            builder = visitor.visit(builder);

            builder.classAsm.mLabel(exit);

            return builder;
        }

        public BlockBuilder<T> out() {
//            classAsm.mLabel(exit);
            BlockBuilder<T> blockBuilder = new BlockBuilder<>(classAsm, type);
            return outBui.visit(blockBuilder);
        }
    }

    public IfElseBuilder ifCall() {
        return new IfElseBuilder(classAsm, type);
    }

    public class WhileBuilder extends Builder<T> {
        Label label = new Label();
        Label exit = new Label();

        public WhileBuilder(ClassAsm classAsm, Class<T> type) {
            super(classAsm, type);
        }

        public BlockBuilder<T> setContent(
                AsmBudVisitor.AsmBlockBuilder<T> condition,
                AsmBudVisitor.IfBuilder<T> visitor,
                int opcode
        ) {
            classAsm.mLabel(exit);

            classAsm.setState("set-content-if");
            BlockBuilder<T> callBuilder = condition.visit(new BlockBuilder<>(classAsm, type));
            callBuilder.classAsm.releaseState();

            callBuilder.classAsm.jumpInsn(opcode, label);

            BlockBuilder<T> ke = visitor.visit(callBuilder);

            ke.classAsm.jumpInsn(Opcodes.GOTO, exit);
            ke.classAsm.mLabel(label);

            return ke;
        }
    }

    public WhileBuilder whileCall() {
        return new WhileBuilder(classAsm, type);
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

    public CallBuilder<T> call(String owner, String var, String type) {
        return call(Opcodes.GETSTATIC, owner, var, type);
    }

    public CallBuilder<T> call(Class<?> owner, String var, Class<?> type) {
        return call(Opcodes.GETSTATIC, owner, var, type);
    }

    public CallBuilder<T> call(int opcode, String owner, String var, String type) {
        return new CallBuilder<>(classAsm,  this.type).call(opcode, owner, var, type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        return new CallBuilder<>(classAsm,  this.type).call(opcode, owner, var, type);
    }

    public CallBuilder<T> callThis(){
        return new CallBuilder<>(classAsm, type).callThis();
    }

//    public ClassBuilder.StaticVarBuilder<T> declareStaticVar(String name, Class<?> returnType) {
//        ClassBuilder.StaticVarBuilder<T> builder = new ClassBuilder.StaticVarBuilder<>(classAsm, type);
//        builder.setClassVars(name, returnType);
//
//        return builder;
//    }
//
//    public ClassBuilder.StaticVarBuilder declareStaticVar(String name, String returnType) {
//        ClassBuilder.StaticVarBuilder builder = new ClassBuilder.StaticVarBuilder(classAsm, type);
//        builder.setClassVars(name, Format.formatStrPack(returnType));
//
//        return builder;
//    }

    public CallBuilder<T> callClassVar(String var, String type) {
        return new CallBuilder<>(classAsm, this.type).callClassVar(var, type);
    }

    public CallBuilder<T> callClassVar(String var, Class<?> type) {
        return new CallBuilder<>(classAsm, this.type).callClassVar(var, type);
    }

    public T out() {
        return create();
    }
}
