package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class Builder<T> {
    protected final ClassAsm classAsm;

    protected final Class<T> type;

    public Builder(ClassAsm classAsm, Class<T> type) {
        this.classAsm = classAsm;
        this.type = type;
    }

    public Class<T>  getType() {
        return type;
    }

    public ClassAsm getClassAsm() {
        return classAsm;
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

    protected T create(Object... args){
        List<Class<?>> classes = new ArrayList<>();

        for (Object arg : args) {
            classes.add(arg.getClass());
        }

        try {
            return type.getConstructor(classes.toArray(new Class[0])).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
