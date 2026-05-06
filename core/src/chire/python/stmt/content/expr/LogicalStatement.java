package chire.python.stmt.content.expr;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

public class LogicalStatement extends PyStatement {
    public final PyStatement left;
    public final Token operator;
    public final String operatorStr;
    public final PyStatement right;

    public LogicalStatement(PyStatement left, Token operator, PyStatement right){
        this.left = left;
        this.operator = operator;
        this.right = right;

        this.operatorStr = null;
    }

    public LogicalStatement(PyStatement left, String operator, PyStatement right){
        this.left = left;
        this.operatorStr = operator;
        this.right = right;

        this.operator = null;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder<?>) builder).callMethod(JPUtil.class, "operation", new Class[]{Object.class, Object.class, String.class}, Object.class)
                    .setContent(logiBui -> logiBui.definitPar(
                            logiPar -> (CallBuilder) left.build(logiPar),
                            logiPar -> (CallBuilder) right.build(logiPar),
                            logiPar -> logiPar.definitObj(operator != null ? operator.getText() : operatorStr)
                    ));
        } else if (builder instanceof CallBuilder<?>) {
            return ((BlockBuilder<?>) ((CallBuilder<?>) builder)._break()).callMethod(JPUtil.class, "operation", new Class[]{Object.class, Object.class, String.class}, Object.class)
                    .setContent(logiBui -> logiBui.definitPar(
                            logiPar -> (CallBuilder) left.build(logiPar),
                            logiPar -> (CallBuilder) right.build(logiPar),
                            logiPar -> logiPar.definitObj(operator != null ? operator.getText() : operatorStr)
                    ));
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Logi{").indent()
                .newLine().add("left:").indent();
        left.toString(indenter);
        indenter.unindent().newLine().add("operator=")
                .add(operator != null ? operator.getText() : operatorStr)
                .newLine().add("right:").indent();
        right.toString(indenter);
        indenter.unindent().newLine().unindent().add("}");
    }
}
