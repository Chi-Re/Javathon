package chire.asm;

import chire.asm.args.Args;
import chire.asm.util.Format;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ClassAsm {
    private ClassWriter cw;

    private MethodVisitor mv = null;

    private final Map<String, Integer> varsKey = new HashMap<>();

    private String className;

    public ClassAsm() {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public void defineClass(String className, Class<?> superClass) {
        this.className = className.replace('.', '/');
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, Format.formatPack(superClass, false), null);
    }

    public void defineFunction(int access, String name, Args args, Class<?> returnType) {
        mv = cw.visitMethod(access, name,
                Format.formatArgs(args, returnType),
                null, null);

        for (String var : args.getArgNames()) {
            varsKey.put(var, varsKey.size()+1);
        }

        mv.visitCode();
    }

    public void defineClassVar(int access, String name, Class<?> returnType) {
        FieldVisitor fv = cw.visitField(access, name, Format.formatPack(returnType), null, null);
        fv.visitEnd();
    }

    public void classVarInsn(int opcode, String name, Class<?> type, Object value) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(value);
        mv.visitFieldInsn(opcode, this.className, name, Format.formatPack(type));
    }

    public void invokeVar(int opcode, Class<?> owner, String name, String type){
        //TODO 对于解析时type的获取明显是有些麻烦且不符合直觉的，添加自动获取type的功能
        mv.visitFieldInsn(opcode, Format.formatPack(owner, false), name, type);
    }

    public void invokeMethod(int opcode, Class<?> owner, String name, String type){
        mv.visitMethodInsn(opcode, Format.formatPack(owner, false), name, type, false);
    }

    public void varInsn(String name) {
        if (varsKey.containsKey(name)) {
            mv.visitVarInsn(ASTORE, varsKey.get(name));
        } else {
            varsKey.put(name, varsKey.size());
            mv.visitVarInsn(ASTORE, varsKey.size());
        }
    }

    public void ldcInsn(Object obj){
        mv.visitLdcInsn(obj);
    }

    public void toReturn(boolean returnValue) {
        if (returnValue) {
            mv.visitInsn(ARETURN);
        } else {
            mv.visitInsn(RETURN);
        }
    }

    public void toReturn() {
        toReturn(false);
    }

    public void returnBlock(){
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public void closeClass(){
        cw.visitEnd();
    }

    public byte[] getByte(){
        return cw.toByteArray();
    }
}
