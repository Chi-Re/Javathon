package chire.python.stmt.content.decl;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

public class IndexStatement extends PyStatement {
    private final Token name;
    private final PyStatement index;

    public IndexStatement(Token name, PyStatement index) {
        this.name = name;
        this.index = index;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder<?>) builder).callMethod(JPUtil.class, "getSerial", new Class[]{Object.class, Object.class}, Object.class).setContent(methBui -> {
                return methBui.definitPar(
                        par -> {
                            if (builder.getType().equals(ClinitDefinition.class)) {
                                return par.callStatic(name.getText(), Object.class);
                            }

                            try {
                                return par.callLocal(name.getText());
                            } catch (Exception e) {
                                return par.callStatic(name.getText(), Object.class);
                            }
                        },
                        par -> (CallBuilder) index.build(par)
                );
            });
        } else if (builder instanceof CallBuilder<?>) {
            return ((CallBuilder) builder)._break().callMethod(JPUtil.class, "getSerial", new Class[]{Object.class, Object.class}, Object.class).setContent(methBui -> {
                return methBui.definitPar(
                        par -> {
                            if (builder.getType().equals(ClinitDefinition.class)) {
                                return par.callStatic(name.getText(), Object.class);
                            }

                            try {
                                return par.callLocal(name.getText());
                            } catch (Exception e) {
                                return par.callStatic(name.getText(), Object.class);
                            }
                        },
                        par -> (CallBuilder) index.build(par)
                );
            });
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Index{").newLine()
                .indent()
                .add("name=").add(name.getText()).newLine()
                .add("index:").indent();
        if (index != null) index.toString(indenter);

//            indenter.newLine().unindent().add("content:").indent();
//            if (content != null) content.toString(indenter);
        indenter.unindent().newLine()
                .unindent()
                .add("}");
    }
}
