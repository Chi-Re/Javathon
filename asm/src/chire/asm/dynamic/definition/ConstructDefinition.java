package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ConstructDefinition extends BlockBuilder<ConstructDefinition> {
    public ConstructDefinition(ClassAsm classAsm) {
        super(classAsm, ConstructDefinition.class);
    }

    @Override
    public VarBuilder setVar(int opcode, String name, Class<?> type) {
        return (VarBuilder) super.setVar(opcode, name, type);
    }

    @Override
    public VarBuilder setClassVar(int opcode, String name, Class<?> type) {
        return (VarBuilder) super.setClassVar(opcode, name, type);
    }

    public ClassBuilder _back(){
        classAsm.toReturn();
        return new ClassBuilder(classAsm);
    }

    public static class VarBuilder extends BlockBuilder.VarBuilder<ConstructDefinition> {
        public VarBuilder(ClassAsm classAsm) {
            super(classAsm, ConstructDefinition.class);
        }
    }
}
