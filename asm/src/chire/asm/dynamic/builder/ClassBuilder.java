package chire.asm.dynamic.builder;

import chire.asm.args.Args;
import chire.asm.AsmBuddy;
import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.ConstructDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.OpcodesFormat;
import org.objectweb.asm.Opcodes;

public class ClassBuilder extends Builder<AsmBuddy> {
    public ClassBuilder(ClassAsm classAsm) {
        super(classAsm, AsmBuddy.class);
    }

    public static class VarBuilder extends Builder<AsmBuddy> {
        private int access;
        private String name;
        private Class<?> returnType;

        public VarBuilder(ClassAsm classAsm, Class<AsmBuddy> type) {
            super(classAsm, type);
        }

        private void setClassVars(int access, String name, Class<?> returnType) {
            this.access = access;
            this.name = name;
            this.returnType = returnType;
        }

        public ClassBuilder setContent(AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content) {
            this.classAsm.addClassVars(new ClassVarBuild(OpcodesFormat.to(access), name, returnType, content));
            return new ClassBuilder(classAsm);
        }
    }

    public static class ClassVarBuild {
        private final int access;
        private final String name;
        private final Class<?> returnType;
        private final AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content;

        public ClassVarBuild(int access, String name, Class<?> returnType, AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content) {
            this.access = access;
            this.name = name;
            this.returnType = returnType;
            this.content = content;
        }

        public FunctionDefinition visit(FunctionDefinition builder) {
            return builder.setClassVar(access, name, returnType).setContent(content);
        }
    }

    public VarBuilder declareVar(int access, String name, Class<?> returnType) {
        classAsm.defineClassVar(access, name, returnType);

        VarBuilder builder = new VarBuilder(classAsm, type);
        builder.setClassVars(access, name, returnType);

        return builder;
    }

    public VarBuilder declareVar(String name, Class<?> returnType) {
        return declareVar(Opcodes.ACC_PUBLIC, name, returnType);
    }

    public ClassBuilder defineVar(int access, String name, Class<?> returnType) {
        classAsm.defineClassVar(access, name, returnType);

        return new ClassBuilder(classAsm);
    }

    public ClassBuilder defineVar(String name, Class<?> returnType){
        return defineVar(Opcodes.ACC_PUBLIC, name, returnType);
    }

    public ConstructDefinition defineConstruct(int access, Args args){
        //TODO 补丁，之后新添加一个Builder
        classAsm.defineConstruct(access, args, Object.class, "()V");

        return new ConstructDefinition(classAsm);
    }

    public ConstructDefinition defineConstruct(){
        return defineConstruct(Opcodes.ACC_PUBLIC, new Args());
    }

    public ClinitDefinition defineClinit(int access){
        classAsm.defineClinit(access);

        return new ClinitDefinition(classAsm);
    }

    public ClinitDefinition defineClinit(){
        return defineClinit(Opcodes.ACC_PUBLIC);
    }

    public FunctionDefinition defineFunction(int access, String name, Args args, Class<?> returnType) {
        classAsm.defineFunction(access, name, args, returnType);

        return new FunctionDefinition(classAsm);
    }

    public FunctionDefinition defineFunction(int access, String name, Args args) {
        return defineFunction(access, name, args, null);
    }

    public FunctionDefinition defineFunction(int access, String name) {
        return defineFunction(access, name, new Args());
    }

    public FunctionDefinition defineFunction(String name) {
        return defineFunction(Opcodes.ACC_PUBLIC, name, new Args());
    }

    public AsmBuddy make(){
        FunctionDefinition builder = defineFunction(Opcodes.ACC_PRIVATE, "$__init__$FieldInsn$", new Args(), null);

        for (ClassVarBuild callBuilder : classAsm.getClassVars()) {
            builder = callBuilder.visit(builder);
        }

        builder.classAsm.toReturn();
        builder.classAsm.closeClass();

        return new AsmBuddy(builder.classAsm);
    }

//    public byte[] make() {
//        classAsm.closeClass();
//
//        return classAsm.getByte();
//    }
}
