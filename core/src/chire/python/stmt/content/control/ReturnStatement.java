package chire.python.stmt.content.control;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.python.stmt.PyStatement;
import chire.python.stmt.type.NoneStatement;
import chire.python.util.SmartIndenter;

public class ReturnStatement extends PyStatement {
    public final PyStatement returnStmt;

    public ReturnStatement(PyStatement returnStmt){
        this.returnStmt = returnStmt;
    }

    public ReturnStatement(){
        this.returnStmt = new NoneStatement();
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof FunctionDefinition) {
            if (returnStmt != null) {
                return ((FunctionDefinition) builder)._return(ret -> {
                    Builder<FunctionDefinition> res = (Builder<FunctionDefinition>) returnStmt.build(builder);

                    if (res instanceof CallBuilder<FunctionDefinition>) {
                        return ((CallBuilder<FunctionDefinition>) res)._break();
                    } else if (res instanceof BlockBuilder<FunctionDefinition>) {
                        return ((BlockBuilder<FunctionDefinition>) res).out();
                    }

                    throw new RuntimeException("no key");
                });
            } else {
                return ((FunctionDefinition) builder)._return(re -> re.definitObj(null)._break());
            }
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Return{")
                .indent();
        returnStmt.toString(indenter);
        indenter.newLine()
                .unindent()
                .add("}");
    }
}