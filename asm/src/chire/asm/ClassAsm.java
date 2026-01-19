package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.VarVisitor;
import chire.asm.util.Format;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class ClassAsm {
    private ClassWriter cw;

    private MethodVisitor mv = null;

    private final Map<String, Integer> varsKey = new HashMap<>();

    private final List<VarVisitor> classVars = new ArrayList<>();

    private String className;

    private Class<?> superClass;

    private boolean initialize = false;

    public ClassAsm(String className, Class<?> superClass) {
        this.className = className.replace('.', '/');
        this.superClass = superClass;
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, Format.formatPack(superClass, false), null);
    }

    public void defineClass(){
        cw.newClass("");
    }

    public void defineConstruct(int access, Args args, Class<?> owner, String type) {
        defineFunction(access, "<init>", args, null);

        mv.visitVarInsn(ALOAD, 0);
        invokeMethod(Opcodes.INVOKESPECIAL, owner, "<init>", type);

        mv.visitVarInsn(ALOAD, 0);
        invokeMethod(Opcodes.INVOKESPECIAL, this.className, "$__init__$FieldInsn$", "()V");

        initialize = true;
    }

    public void defineClinit(int access) {
        defineFunction(access, "<clinit>", new Args(), null);
    }

    public void defineFunction(int access, String name, Args args, Class<?> returnType) {
        mv = cw.visitMethod(access, name, Format.formatArgs(args, returnType), null, null);

        for (String var : args.getArgNames()) {
            varsKey.put(var, varsKey.size()+1);
        }

        mv.visitCode();
    }

    public void defineClassVar(int access, String name, Class<?> returnType, VarVisitor varVisitor) {
        FieldVisitor fv = cw.visitField(access, name, Format.formatPack(returnType)+";", null, null);
        fv.visitEnd();

        classVars.add(varVisitor);
    }

    public void classVarInsn(int opcode, String name, Class<?> type, Object value) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(value);
        mv.visitFieldInsn(opcode, this.className, name, Format.formatPack(type)+";");
    }

    public void invokeVar(int opcode, Class<?> owner, String name, String type){
        //TODO 对于解析时type的获取明显是有些麻烦且不符合直觉的，添加自动获取type的功能
        mv.visitFieldInsn(opcode, Format.formatPack(owner, false), name, type);
    }

    public void invokeMethod(int opcode, String owner, String name, String type){
        mv.visitMethodInsn(opcode, owner, name, type, false);
    }

    public void invokeMethod(int opcode, Class<?> owner, String name, String type){
        invokeMethod(opcode, Format.formatPack(owner, false), name, type);
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
        defineFunction(ACC_PRIVATE, "$__init__$FieldInsn$", new Args(), null);

        for (VarVisitor v : classVars) {
            v.init(this);
        }

        classVars.clear();
        toReturn();
        returnBlock();

        if (!initialize) {
            defineConstruct(ACC_PUBLIC, new Args(), this.superClass, "()V");
            toReturn();
            returnBlock();

            initialize = true;
        }

        cw.visitEnd();
    }

    public byte[] getByte(){
        return cw.toByteArray();
    }
}
