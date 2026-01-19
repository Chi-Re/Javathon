package chire.asm.dynamic.builder;

import chire.asm.args.Args;
import chire.asm.AsmBuddy;
import chire.asm.ClassAsm;
import chire.asm.dynamic.VarVisitor;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.ConstructDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import org.objectweb.asm.Opcodes;

public class ClassBuilder extends Builder<AsmBuddy> {
    public ClassBuilder(ClassAsm classAsm) {
        super(classAsm, AsmBuddy.class);
    }

    public ClassBuilder defineVar(int access, String name, Class<?> returnType, Object value) {
        classAsm.defineClassVar(access, name, returnType, new VarVisitor() {
            public void init(ClassAsm mv) {
                mv.classVarInsn(Opcodes.PUTFIELD, name, returnType, value);
            }
        });

        return new ClassBuilder(classAsm);
    }

    public ClassBuilder defineVar(String name, Class<?> returnType, Object value){
        return defineVar(Opcodes.ACC_PUBLIC, name, returnType, value);
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

    public byte[] make() {
        classAsm.returnBlock();

        classAsm.closeClass();

        return classAsm.getByte();
    }
}
