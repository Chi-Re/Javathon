package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;

import java.lang.reflect.InvocationTargetException;

public abstract class Builder<T> {
    protected final ClassAsm classAsm;

    protected final Class<T> type;

    public Builder(ClassAsm classAsm, Class<T> type) {
        this.classAsm = classAsm;
        this.type = type;
    }

    protected T create(){
        try {
            return this.type.getDeclaredConstructor(classAsm.getClass(), Class.class).newInstance(classAsm, type);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            try {
                return this.type.getDeclaredConstructor(classAsm.getClass()).newInstance(classAsm);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
