package chire.asm.dynamic.builder;

import chire.asm.args.Args;
import chire.asm.AsmBuddy;
import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.ConstructDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import chire.asm.util.OpcodesFormat;
import org.objectweb.asm.Opcodes;

public class ClassBuilder extends Builder<AsmBuddy> {
    public ClassBuilder(ClassAsm classAsm) {
        super(classAsm, AsmBuddy.class);
    }

    public static class VarBuilder extends Builder<AsmBuddy> {
        private int access;
        private String name;
        private String returnType;

        public VarBuilder(ClassAsm classAsm, Class<AsmBuddy> type) {
            super(classAsm, type);
        }

        private void setClassVars(int access, String name, String returnType) {
            this.access = access;
            this.name = name;
            this.returnType = returnType;
        }

        private void setClassVars(int access, String name, Class<?> returnType) {
            setClassVars(access, name, Format.formatPack(returnType));
        }

        public ClassBuilder setContent(AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content) {
            this.classAsm.addClassVars(new ClassVarBuild(OpcodesFormat.to(access), name, returnType, content));
            return new ClassBuilder(classAsm);
        }
    }

    public static class StaticVarBuilder extends Builder<AsmBuddy> {
        private String name;
        private String returnType;

        public StaticVarBuilder(ClassAsm classAsm, Class<AsmBuddy> type) {
            super(classAsm, type);
        }

        private void setClassVars(String name, String returnType) {
            this.name = name;
            this.returnType = returnType;
        }

        private void setClassVars(String name, Class<?> returnType) {
            setClassVars(name, Format.formatPack(returnType));
        }

        public ClassBuilder setContent(AsmBudVisitor.AsmCallBuilder<ClinitDefinition> content) {
            this.classAsm.addClassVars(new StaticVarBuild(name, returnType, content));
            return new ClassBuilder(classAsm);
        }
    }

    public static class ClassVarBuild {
        private final int access;
        private final String name;
        private final String returnType;
        private final AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content;

        public ClassVarBuild(int access, String name, String returnType, AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content) {
            this.access = access;
            this.name = name;
            this.returnType = returnType;
            this.content = content;
        }

        public ClassVarBuild(int access, String name, Class<?> returnType, AsmBudVisitor.AsmCallBuilder<FunctionDefinition> content) {
            this(access, name, Format.formatPack(returnType), content);
        }

        public FunctionDefinition visit(FunctionDefinition builder) {
            return builder.setClassVar(access, name, returnType).setContent(content);
        }
    }

    public static class StaticVarBuild {
        private final String name;
        private final String returnType;
        private final AsmBudVisitor.AsmCallBuilder<ClinitDefinition> content;

        public StaticVarBuild(String name, String returnType, AsmBudVisitor.AsmCallBuilder<ClinitDefinition> content) {
            this.name = name;
            this.returnType = returnType;
            this.content = content;
        }

        public StaticVarBuild(String name, Class<?> returnType, AsmBudVisitor.AsmCallBuilder<ClinitDefinition> content) {
            this(name, Format.formatPack(returnType), content);
        }

        public ClinitDefinition visit(ClinitDefinition builder) {
            return builder.setStaticVar(name, returnType).setContent(content);
        }
    }

    public VarBuilder declareVar(int access, String name, String returnType) {
        classAsm.defineClassVar(access, name, returnType);

        VarBuilder builder = new VarBuilder(classAsm, type);
        builder.setClassVars(access, name, returnType);

        return builder;
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

    public StaticVarBuilder declareStaticVar(String name, Class<?> returnType) {
        classAsm.defineClassVar(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, returnType);

        StaticVarBuilder builder = new StaticVarBuilder(classAsm, type);
        builder.setClassVars(name, returnType);

        return builder;
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
        return defineClinit(Opcodes.ACC_STATIC);
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

//        builder = builder._return().defineFunction(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "$import$ACC_STATIC$", new Args(), null);

        ClinitDefinition clinitBuilder = builder._return().defineClinit();

        for (StaticVarBuild callBuilder : classAsm.getClassStaticVars()) {
            clinitBuilder = callBuilder.visit(clinitBuilder);
        }

        clinitBuilder.classAsm.toReturn();

        clinitBuilder.classAsm.closeClass();

        return new AsmBuddy(clinitBuilder.classAsm);
    }

//    public byte[] make() {
//        classAsm.closeClass();
//
//        return classAsm.getByte();
//    }
}
