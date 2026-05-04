package chire.asm.dynamic.definition;

import chire.asm.ClassAsm;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.ClassBuilder;

public class FunctionDefinition extends BlockBuilder<FunctionDefinition> {
    public FunctionDefinition(ClassAsm classAsm) {
        super(classAsm, FunctionDefinition.class);
    }

    public ClassBuilder _return(boolean retu) {
        classAsm.end(retu);

        return new ClassBuilder(classAsm);
    }

    @Override
    public VarBuilder<FunctionDefinition> setVar(String name) {
        return super.setVar(name);
    }

    @Override
    public ClassVarBuilder<FunctionDefinition> setClassVar(int opcode, String name, Class<?> type) {
        return super.setClassVar(opcode, name, type);
    }

    public ClassBuilder _return() {
        return _return(false);
    }

    public ClassBuilder _return(AsmBudVisitor.AsmBlockBuilder<FunctionDefinition> builder) {
        this.classAsm.setState("set-content-return");
        FunctionDefinition functionDefinition = builder.visit(this);
        functionDefinition.classAsm.releaseState();

        return functionDefinition._return(true);
    }
}
