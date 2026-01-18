package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;

import java.lang.reflect.InvocationTargetException;

public class BlockBuilder<T> extends Builder {
    private final Class<T> type;

    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm);
        this.type = type;
    }

    public T definitObj(Object obj){
        classAsm.ldcInsn(obj);

        return create();
    }

    public T toVar(String name){
        classAsm.toVar(name);

        return create();
    }

    private T create(){
        try {
            return this.type.getDeclaredConstructor(classAsm.getClass()).newInstance(classAsm);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
