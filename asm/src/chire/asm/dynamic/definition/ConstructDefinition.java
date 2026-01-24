package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ConstructDefinition extends BlockBuilder<ConstructDefinition> {
    public ConstructDefinition(ClassAsm classAsm) {
        super(classAsm, ConstructDefinition.class);
    }

    @Override
    public VarBuilder<ConstructDefinition> setVar(int opcode, String name, Class<?> type) {
        return super.setVar(opcode, name, type);
    }

    @Override
    public VarBuilder<ConstructDefinition> setClassVar(int opcode, String name, Class<?> type) {
        return super.setClassVar(opcode, name, type);
    }

    public ClassBuilder _back(){
        classAsm.toReturn();
        return new ClassBuilder(classAsm);
    }
}
