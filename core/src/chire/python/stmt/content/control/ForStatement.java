package chire.python.stmt.content.control;

import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.asm.ModuleBuilder;
import chire.python.lib.builtins.PyList;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class ForStatement extends PyStatement {
    private final Token variable;
    private final PyStatement iterable;
    private final ArrayList<PyStatement> body;

    public ForStatement(Token variable, PyStatement iterable, ArrayList<PyStatement> body) {
        this.variable = variable;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        AsmBudVisitor.AsmBlockBuilder blockBuilder = content -> {
            return content.setVar("for$"+variable.getText()).setContent(forVar -> {
                        return forVar.callMethod(JPUtil.class, "iterator", new Class[]{Object.class}, PyList.class).setContent(iterBui -> {
                            return iterBui.definitPar(
                                    parbui -> (CallBuilder) iterable.build(parbui)
                            );
                        });
                    })
                    .whileCall().setContent(
                            pd ->
                                    pd.callLocal("for$"+variable.getText())
                                            .callMethod(PyList.class, "hasNext", new Class[]{}, boolean.class)
                                            .setContent(CallBuilder.ParameterBuilder::definitPar)
                                            ._break(),
                            whiCont -> {
                                whiCont = whiCont.setVar(variable.getText()).setContent(vatBui -> vatBui.callLocal("for$"+variable.getText())
                                        .callMethod(PyList.class, "next", new Class[]{}, Object.class)
                                        .setContent(CallBuilder.ParameterBuilder::definitPar)).out();

                                for (PyStatement statement : this.body) {
                                    Builder<?> bui = statement.build(whiCont);
                                    if (bui instanceof CallBuilder<?>) {
                                        whiCont = ((CallBuilder) bui)._break();
                                    } else {
                                        whiCont = (BlockBuilder) bui;
                                    }
                                }

                                return whiCont;
                            },
                            Opcodes.IFEQ
                    )
                    .out();
        };

        if (builder instanceof ClassBuilder) {
            ClassBuilder cont = ((ClassBuilder) builder).setContent(blockBuilder);

            return builder instanceof ModuleBuilder ? new ModuleBuilder(cont.getClassAsm()) : cont;
        } else if (builder instanceof BlockBuilder<?>) {
            return blockBuilder.visit((BlockBuilder<?>) builder);
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("For{").newLine();

        indenter.indent().add("variable:").indent();

        indenter.add(variable.getText());

        indenter.unindent().newLine();
        indenter.indent().add("iterable:").indent();

        iterable.toString(indenter);

        indenter.unindent().newLine()
                .add("body=[")
                .indent();;

        for (PyStatement statement : body) {
            statement.toString(indenter);
        }

        indenter.newLine()
                .unindent()
                .addLine("]")
                .unindent()
                .add("}");
    }
}