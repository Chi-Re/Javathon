package chire.python.stmt.type;

import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.lib.PyList;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;

import java.util.ArrayList;

public class ListStatement extends PyStatement {

    public final ArrayList<PyStatement> list;

    public ListStatement(ArrayList<PyStatement> list){
        this.list = list;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        AsmBudVisitor.AsmCallBuilder[] callBuilders = new AsmBudVisitor.AsmCallBuilder[list.size()];

        for (int i = 0; i < list.size(); i++) {
            int finalI = i;
            callBuilders[i] = builder1 -> (CallBuilder) list.get(finalI).build(builder1);
        }

        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder) builder).callMethod(JPUtil.class, "asPyList", new Class[]{Object[].class}, PyList.class).setContent(funBui -> {
                return funBui.definitPar(callBuilders);
            });
        } else if (builder instanceof CallBuilder<?>) {
            return ((CallBuilder) builder)._break().callMethod(JPUtil.class, "asPyList", new Class[]{Object[].class}, PyList.class).setContent(funBui -> {
                return funBui.definitPar(callBuilders);
            });
        }

        throw new RuntimeException("no key:"+builder.getClass());
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("List[")
                .indent();

        for (PyStatement statement : this.list) {
            statement.toString(indenter);
        }

        indenter.newLine().unindent().add("]");
    }
}