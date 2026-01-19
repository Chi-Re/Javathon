package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;
import chire.asm.util.Format;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;

public class BlockBuilder<T> extends Builder<T> {
    public BlockBuilder(ClassAsm classAsm, Class<T> type) {
        super(classAsm, type);
    }

    public DefinitBuilder<T> definitObj(Object obj) {
        classAsm.ldcInsn(obj);

        return new DefinitBuilder<>(classAsm, type);
    }

    public T setVar(int opcode, String name, Class<?> type, Object value) {
        //TODO 由于字节码操作，这里可能导致错误赋值比如: int a = 1.1F，加判断
        classAsm.classVarInsn(opcode, name, type, value);

        return this.create();
    }

    public T setVar(int opcode, String name, Object value) {
        classAsm.classVarInsn(opcode, name, value.getClass(), value);

        return this.create();
    }

    public T setVar(String name, Class<?> type, Object value) {
        return setVar(Opcodes.PUTFIELD, name, type, value);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, Class<?> type) {
        classAsm.invokeVar(opcode, owner, var, Format.formatPack(type)+";");

        return new CallBuilder<>(classAsm,  this.type);
    }

    public CallBuilder<T> call(int opcode, Class<?> owner, String var, String type) {
        classAsm.invokeVar(opcode, owner, var, type);

        return new CallBuilder<>(classAsm,  this.type);
    }
}
