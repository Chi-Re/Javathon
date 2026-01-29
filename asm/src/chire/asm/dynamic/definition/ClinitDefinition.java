package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ClinitDefinition extends BlockBuilder<ClinitDefinition> {

    public ClinitDefinition(ClassAsm classAsm) {
        super(classAsm, ClinitDefinition.class);
    }

    @Override
    public VarBuilder<ClinitDefinition> setVar(String name) {
        return super.setVar(name);
    }

    @Override
    public ClassVarBuilder<ClinitDefinition> setClassVar(int opcode, String name, Class<?> type) {
        throw new UnsupportedOperationException("Customization is not supported.");
    }

    public ClassVarBuilder<ClinitDefinition> setStaticVar(String name, Class<?> type) {
        return super.setStaticVar(name, type);
    }

    public ClassBuilder _back(){
        classAsm.toReturn();
        return new ClassBuilder(classAsm);
    }
}
