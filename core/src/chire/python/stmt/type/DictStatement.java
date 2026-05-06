package chire.python.stmt.type;

import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.lib.PyDict;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictStatement extends PyStatement {
    private final Map<PyStatement, PyStatement> args;

    public DictStatement(Map<PyStatement, PyStatement> args) {
        this.args = args;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        List<AsmBudVisitor.AsmCallBuilder> list = new ArrayList<>();

        PyStatement[] ks = args.keySet().toArray(new PyStatement[0]);
        PyStatement[] vs = args.values().toArray(new PyStatement[0]);

        for (int i = 0; i < args.size(); i++) {
            int finalI = i;
            list.add(builder1 -> (CallBuilder) ks[finalI].build(builder1));
            list.add(builder1 -> (CallBuilder) vs[finalI].build(builder1));
        }

        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder<?>) builder).callMethod(JPUtil.class, "asPyDict", new Class[]{Object[].class}, PyDict.class).setContent(funBui -> {
                return funBui.definitPar(list.toArray(new AsmBudVisitor.AsmCallBuilder[0]));
            });
        } else if (builder instanceof CallBuilder<?>) {
            return ((CallBuilder<?>) builder)._break().callMethod(JPUtil.class, "asPyDict", new Class[]{Object[].class}, PyDict.class).setContent(funBui -> {
                return funBui.definitPar(list.toArray(new AsmBudVisitor.AsmCallBuilder[0]));
            });
        }

        throw new RuntimeException("no key:"+builder.getClass());
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Dict{")
                .indent();

        for (PyStatement statement : this.args.keySet()) {
            statement.toString(indenter);
            indenter.add(" : ").indent();
            args.get(statement).toString(indenter);
            indenter.unindent().add(",");
        }

        indenter.newLine().unindent().add("}");
    }
}