package chire.asm;

import chire.asm.dynamic.builder.ClassBuilder;

import java.util.Map;

public class AsmBuddy {
    private ClassAsm classAsm;

    public AsmBuddy(String name, Class<?> superClass) {
        this(new ClassAsm(name, superClass));
    }

    public AsmBuddy(ClassAsm classAsm) {
        this.classAsm = classAsm;
    }

    public ClassBuilder create() {
        return new ClassBuilder(classAsm);
    }

    public ClassAsm getClassAsm() {
        return classAsm;
    }

    public Map<String, byte[]> save() {
        return classAsm.getByte();
    }
}
