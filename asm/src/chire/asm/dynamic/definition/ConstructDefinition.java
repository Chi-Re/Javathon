package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class ConstructDefinition extends BlockBuilder<ConstructDefinition> {
    public ConstructDefinition(ClassAsm classAsm) {
        super(classAsm, ConstructDefinition.class);
    }

    public ClassBuilder _back(){
        classAsm.toReturn();
        return new ClassBuilder(classAsm);
    }
}
