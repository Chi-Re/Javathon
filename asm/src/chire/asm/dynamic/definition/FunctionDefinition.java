package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class FunctionDefinition extends BlockBuilder<FunctionDefinition> {
    public FunctionDefinition(ClassAsm classAsm) {
        super(classAsm, FunctionDefinition.class);
    }

    public ClassBuilder _return(boolean retu) {
        classAsm.toReturn(retu);

        return new ClassBuilder(classAsm);
    }

    @Override
    public VarBuilder setVar(int opcode, String name, Class<?> type) {
        return (VarBuilder) super.setVar(opcode, name, type);
    }

    @Override
    public VarBuilder setClassVar(int opcode, String name, Class<?> type) {
        return (VarBuilder) super.setClassVar(opcode, name, type);
    }

    public ClassBuilder _return() {
        return _return(false);
    }

    public static class VarBuilder extends BlockBuilder.VarBuilder<FunctionDefinition> {
        public VarBuilder(ClassAsm classAsm) {
            super(classAsm, FunctionDefinition.class);
        }
    }
}
