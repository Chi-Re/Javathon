package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.builder.ClassBuilder;
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

    protected List<ClassBuilder.ClassVarBuild> classVarBuilds = new ArrayList<>();

    protected List<ClassBuilder.StaticVarBuild> staticVarBuilds = new ArrayList<>();

    private String className;

    private String superClass;

    private boolean initialize = false;

    public ClassAsm(String className, String superClass) {
        this.className = className.replace('.', '/');
        this.superClass = superClass;
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, this.superClass, null);
    }

    public ClassAsm(String className, Class<?> superClass) {
        this(className, Format.formatPack(superClass, false));
    }

//    public ClassAsm defineClass(){
//        return null;
//    }

    public void defineConstruct(int access, Args args, Class<?> owner, String type) {
        defineConstruct(access, args, Format.formatPack(owner, false), type);
    }

    public void defineConstruct(int access, Args args, String owner, String type) {
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
        varsKey.clear();
        mv = cw.visitMethod(access, name, Format.formatArgs(args, returnType), null, null);

        for (String var : args.getArgNames()) {
            varsKey.put(var, varsKey.size()+1);
        }

        mv.visitCode();
    }

    public void defineClassVar(int access, String name, Class<?> returnType) {
        defineClassVar(access, name, Format.formatPack(returnType));
    }

    public void defineClassVar(int access, String name, String returnType) {
        FieldVisitor fv = cw.visitField(access, name, returnType+";", null, null);
        fv.visitEnd();
    }

    public void classVarInsn(int opcode, String name, Class<?> type, Object value) {
        classVarInsn(opcode, name, Format.formatPack(type), value);
    }

    public void classVarInsn(int opcode, String name, String type, Object value) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(value);
        mv.visitFieldInsn(opcode, this.className, name, type+";");
    }

    public void classVarInsn(int opcode, String name, Class<?> type, VarVisitor varVisitor) {
        classVarInsn(opcode, name, Format.formatPack(type), varVisitor);
    }

    public void classVarInsn(int opcode, String name, String type, VarVisitor varVisitor) {
        mv.visitVarInsn(ALOAD, 0);
        varVisitor.init(this);
        mv.visitFieldInsn(opcode, this.className, name, type+";");
    }

    public void thisInsn() {
        mv.visitVarInsn(ALOAD, 0);
    }

    public void invokeClassVarEnd(int opcode, String name, Class<?> type) {
        invokeClassVarEnd(opcode, name, Format.formatPack(type));
    }

    public void invokeClassVarEnd(int opcode, String name, String type) {
        mv.visitFieldInsn(opcode, this.className, name, type+";");
    }

    public void invokeVar(int opcode, String owner, String name, String type){
        mv.visitFieldInsn(opcode, owner, name, type+";");
    }

    public void invokeVar(int opcode, Class<?> owner, String name, Class<?> type){
        //TODO 对于解析时type的获取明显是有些麻烦且不符合直觉的，添加自动获取type的功能
        mv.visitFieldInsn(opcode, Format.formatPack(owner, false), name, Format.formatPack(type)+";");
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

    public void invokeLocalVar(String name) {
        if (varsKey.containsKey(name)) {
            mv.visitVarInsn(ALOAD, varsKey.get(name)+1);
        } else {
            throw new RuntimeException("no key");
        }
    }

    public void mVisitInsn(int opcodes) {
        mv.visitInsn(opcodes);
    }

    public void ldcInsn(Object obj){
        //Java 8不支持var，每个类型都需要特定类型字节码支持。
        if (obj instanceof Boolean) {
            mv.visitInsn((Boolean) obj ? ICONST_1 : ICONST_0);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        } else if (obj instanceof Float) {
            mv.visitLdcInsn(obj);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (obj instanceof Integer) {
            mv.visitIntInsn(SIPUSH, (Integer) obj);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (obj instanceof Double) {
            mv.visitLdcInsn(obj);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        } else {
            mv.visitLdcInsn(obj);
        }
    }
    public void ldcInsns(String type, Object... objs){
        int ICONST_NUM = Opcodes.ICONST_0 + objs.length;

        mv.visitInsn(ICONST_NUM);
        mv.visitTypeInsn(ANEWARRAY, type);

        for (int i = ICONST_0; i < ICONST_NUM; i++) {
            mv.visitInsn(DUP);
            mv.visitInsn(i);
            ldcInsn(objs[i-ICONST_0]);
            mv.visitInsn(AASTORE);
        }
    }

    public void toReturn(boolean returnValue) {
        if (returnValue) {
            mv.visitInsn(ARETURN);
        } else {
            mv.visitInsn(RETURN);
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public void toReturn() {
        toReturn(false);
    }

    public void closeClass(){
        if (!initialize) {
            defineConstruct(ACC_PUBLIC, new Args(), this.superClass, "()V");
            toReturn();

            initialize = true;
        }

        cw.visitEnd();
    }

    public byte[] getByte(){
        return cw.toByteArray();
    }

    public List<ClassBuilder.ClassVarBuild> getClassVars(){
        return classVarBuilds;
    }

    public  List<ClassBuilder.StaticVarBuild> getClassStaticVars(){
        return staticVarBuilds;
    }

    public void addClassVars(ClassBuilder.ClassVarBuild content) {
        classVarBuilds.add(content);
    }

    public void addClassVars(ClassBuilder.StaticVarBuild content) {
        staticVarBuilds.add(content);
    }
}
