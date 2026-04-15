package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ConstructDefinition extends BlockBuilder<ConstructDefinition> {
    public ConstructDefinition(ClassAsm classAsm) {
        super(classAsm, ConstructDefinition.class);
    }

    @Override
    public VarBuilder<ConstructDefinition> setVar(String name) {
        return super.setVar(name);
    }

    @Override
    public ClassVarBuilder<ConstructDefinition> setClassVar(int opcode, String name, Class<?> type) {
        return super.setClassVar(opcode, name, type);
    }

    @Override
    public ClassVarBuilder<ConstructDefinition> setClassVar(int opcode, String name, String type) {
        return super.setClassVar(opcode, name, type);
    }

    public ClassBuilder _back(){
        classAsm.toReturn();
        return new ClassBuilder(classAsm);
    }
}
