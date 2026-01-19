package chire.asm.dynamic.builder;

import chire.asm.args.Args;
import chire.asm.ClassAsm;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

public class CallBuilder<T> extends Builder<T>{
    public CallBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public CallBuilder<T> callMethod(Class<?> owner, String var, Class<?>[] parameters, Class<?> returnType) {
        classAsm.invokeMethod(Opcodes.INVOKEVIRTUAL, owner, var, Format.formatParameter(parameters, returnType));

        return new CallBuilder<>(classAsm, this.type);
    }

    public CallBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new CallBuilder<>(classAsm, type);
    }

    public T out(){
        return this.create();
    }
}
