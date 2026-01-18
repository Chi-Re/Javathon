package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ClinitDefinition extends BlockBuilder<ClinitDefinition> {

    public ClinitDefinition(ClassAsm classAsm) {
        super(classAsm, ClinitDefinition.class);
    }

    public ClassBuilder _back(){
        classAsm.returnBlock();
        return new ClassBuilder(classAsm);
    }
}
