package chire.python.stmt.content.decl;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.lib.escape.JPArgs;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

public class ParametersStatement extends PyStatement {
    private final Token name;
    private final PyStatement content;

    public ParametersStatement(Token name, PyStatement content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder) builder).callClass(JPArgs.JPArg.class, new Class[]{String.class, Object.class}).setContent(clazzCont -> {
                return clazzCont.definitPar(
                        parBui -> parBui.definitObj(name.getText()),
                        parBui -> (CallBuilder) content.build(parBui)
                );
            });
        } else if (builder instanceof CallBuilder<?>) {
            return ((CallBuilder) builder).callClass(JPArgs.JPArg.class, new Class[]{String.class, Object.class}).setContent(clazzCont -> {
                return clazzCont.definitPar(
                        parBui -> parBui.definitObj(name.getText()),
                        parBui -> (CallBuilder) content.build(parBui)
                );
            });
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().addLine("Par{")
                .indent()
                .add("name=").addLine(name.getText())
                .add("content=[");

        indenter.indent();
        content.toString(indenter);
        indenter.unindent().newLine();

        indenter.addLine("]")
                .unindent()
                .add("}");
    }
}