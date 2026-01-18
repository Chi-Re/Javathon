package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;

public class DefinitBuilder<T> extends Builder<T> {
    public DefinitBuilder(ClassAsm classAsm, Class<T> clazz) {
        super(classAsm, clazz);
    }

    public T toVar(String name){
        classAsm.toVar(name);

        return this.create();
    }
}
