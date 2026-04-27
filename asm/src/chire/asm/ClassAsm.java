package chire.asm;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.util.CallLogger;
import chire.asm.util.Format;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.objectweb.asm.*;

import java.lang.reflect.InvocationTargetException;
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

    protected List<ClassBuilder.StaticBuild> staticVarBuilds = new ArrayList<>();

    public final String className;

    public final String superClass;

    private boolean initialize = false;

    private final ClassAsm outer;

    private final List<String> levelStack = new ArrayList<>();

    static CallLogger logger = new CallLogger();

    public ClassAsm(String className, String superClass) {
        this(className, superClass, null);
    }

    public ClassAsm(String className, String superClass, ClassAsm outer) {
        this.className = className.replace('.', '/');
        this.superClass = superClass;
        this.outer = outer;
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, this.className, null, this.superClass, null);

        if (outer != null) {
            String[] nes = outer.className.split("\\$");
            String[] nes2 = className.split("\\$");
            cw.visitInnerClass(nes[nes.length-1]+"$"+nes2[nes2.length-1], nes[nes.length-1], nes2[nes2.length-1], ACC_PUBLIC | ACC_STATIC);
        }
    }

    public ClassAsm(String className, Class<?> superClass) {
        this(className, superClass, null);
    }

    public ClassAsm(String className, Class<?> superClass, ClassAsm outer) {
        this(className, Format.formatPack(superClass, false), outer);
    }

    public static ClassAsm create(Class<?>[] types, Object[] args) {
        try {
            return new ByteBuddy()
                    .subclass(ClassAsm.class)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(logger))
                    .make()
                    .load(ClassAsm.class.getClassLoader())
                    .getLoaded()
                    .getConstructor(types)
                    .newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassAsm defineClass(String className, Class<?> superClass){
        return defineClass(className, Format.formatPack(superClass, false));
    }

    public ClassAsm defineClass(String className, String superClass){
        String[] nes = this.className.split("\\$");
        String[] nes2 = className.split("\\$");
        cw.visitInnerClass(nes[nes.length-1]+"$"+nes2[nes2.length-1], nes[nes.length-1], nes2[nes2.length-1], ACC_PUBLIC | ACC_STATIC);

        return new ClassAsm(this.className+"$"+className, superClass, this);
    }

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
            varsKey.put(var, varsKey.size());
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

    public void invokeStaticVar(int opcode, String name, Class<?> type) {
        invokeStaticVar(opcode, name, Format.formatPack(type));
    }

    public void invokeStaticVar(int opcode, String name, String type) {
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

    public void newClass(Class<?> name) {
        newClass(Format.formatStrType(name));
    }

    public void newClass(String name) {
        mv.visitTypeInsn(NEW, name);
        mv.visitInsn(DUP);
    }

    public void varInsn(String name) {
        varInsn(ASTORE, name);
    }

    public void varInsn(int opcode, String name) {
        if (varsKey.containsKey(name)) {
            mv.visitVarInsn(opcode, varsKey.get(name));
        } else {
            varsKey.put(name, varsKey.size());
            mv.visitVarInsn(opcode, varsKey.size()-1);
        }
    }

    public void invokeLocalVar(String name) {
        if (varsKey.containsKey(name)) {
            mv.visitVarInsn(ALOAD, varsKey.get(name));
        } else {
            throw new RuntimeException("no key");
        }
    }

    public void invokeThis() {
        mv.visitVarInsn(ALOAD, 0);
    }

    public void mVisitInsn(int opcodes) {
        mv.visitInsn(opcodes);
    }

    public void mVisitTypeInsn(final int opcode, final String type) {
        mv.visitTypeInsn(opcode, type);
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

    public void jumpInsn(int opcod, Label label) {
        mv.visitJumpInsn(opcod, label);
    }

    public void mLabel(Label label) {
        mv.visitLabel(label);
    }

    public void mFrame(Object[] objects) {
        mv.visitFrame(Opcodes.F_APPEND, 2, objects, 0, null);
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

    public Map<String, byte[]> getByte(){
        HashMap<String, byte[]> bytes = new HashMap<>();

        ClassAsm classAsm = this;

        for (;;) {
            bytes.put(classAsm.className, classAsm.cw.toByteArray());

            classAsm = classAsm.outer;

            if (classAsm == null) break;
        }

        return bytes;
    }

    public ClassAsm getOuter() {
        return outer;
    }

    public List<ClassBuilder.ClassVarBuild> getClassVars(){
        return classVarBuilds;
    }

    public  List<ClassBuilder.StaticBuild> getClassStaticVars(){
        return staticVarBuilds;
    }

    public void addClassVars(ClassBuilder.ClassVarBuild content) {
        classVarBuilds.add(content);
    }

    public void addClassVars(ClassBuilder.StaticBuild content) {
        staticVarBuilds.add(content);
    }

    public void setState(String state) {
        this.levelStack.add(state);
    }

    public void setState(String state, Runnable runnable) {
        setState(state);
        runnable.run();
        releaseState();
    }

    public String getState() {
        if (levelStack.isEmpty()) return "null";
        return this.levelStack.get(this.levelStack.size()-1);
    }

    public void releaseState() {
        this.levelStack.remove(this.levelStack.size()-1);
    }
}
