package chire.python.stmt.content.expr;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;

public class SubSetStatement extends PyStatement {

    public final PyStatement key;

    public final PyStatement call;

    public final PyStatement var;

    public SubSetStatement(PyStatement key, PyStatement call, PyStatement var) {
        this.key = key;
        this.call = call;
        this.var = var;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof ClassBuilder) {
            return ((ClassBuilder) builder).setContent(contBui -> {
                return makeSubSet(contBui).out();
            });
        } else if (builder instanceof BlockBuilder<?>){
            return makeSubSet((BlockBuilder<?>) builder);
        }

        return super.build(builder);
    }

    public <T extends BlockBuilder<T>> BlockBuilder<T> makeSubSet(BlockBuilder<T> builder) {
        if (call instanceof VarCallStatement) {
            return builder.callMethod(JPUtil.class, "setVar", new Class[]{Object.class, String.class, Object.class}, null).setContent(metbui -> {
                return metbui.definitPar(
                        parBui -> ((CallBuilder<?>) key.build(builder)),
                        parBui -> parBui.definitObj(((VarCallStatement) call).name.getText()),
                        parBui -> (CallBuilder<?>) var.build(parBui)
                );
            })._break();
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("SubSet{")
                .indent()
                .newLine().add("key:").indent();
        key.toString(indenter);

        indenter.unindent().newLine().add("call:").indent();

        call.toString(indenter);

        indenter.unindent().newLine().add("var:").indent();

        var.toString(indenter);

        indenter.unindent().newLine().unindent().add("}");
    }
}