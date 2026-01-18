package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;

import java.lang.reflect.InvocationTargetException;

public class BlockBuilder<T> extends Builder<T> {
    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public DefinitBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new DefinitBuilder<>(classAsm, type);
    }
}
