package chire.python.asm;

import chire.asm.AsmBuddy;
import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.ClassBuilder;

public class PythonAsmBuddy extends AsmBuddy {
    public PythonAsmBuddy(ClassAsm classAsm) {
        super(classAsm);
    }

    public PythonAsmBuddy(String name, Class<?> superClass) {
        super(name, superClass);
    }

    @Override
    public ClassBuilder create() {
        return new ModuleBuilder(getClassAsm());
    }
}
