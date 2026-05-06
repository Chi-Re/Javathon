package chire.python.stmt.content;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.util.Format;
import chire.python.asm.ModuleBuilder;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;
import chire.python.stmt.block.*;
import chire.python.stmt.type.*;

/**定义变量*/
public class VarStatement extends PyStatement {
    public final Token name;

    public PyStatement value;

    public FunStatement.TypeStatement type;
    private final PyStatement index;

    public VarStatement(Token name, PyStatement value, FunStatement.TypeStatement type) {
        this(name, null, value, type);
    }

    public VarStatement(Token name, PyStatement index, PyStatement value, FunStatement.TypeStatement type) {
        if (name == null) throw new RuntimeException("name 不能为空");
        this.name = name;
        this.index = index;
        this.value = value;
        this.type = type;
    }

    public VarStatement(Token name, PyStatement value) {
        this(name, null, value, null);
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (index != null) {
            if (builder instanceof ClassBuilder) {
                ClassBuilder classBuilder = ((ClassBuilder) builder).setContent(clazzBui -> {
                    return clazzBui.callMethod(JPUtil.class, "setSerial", new Class[]{Object.class, Object.class, Object.class}, null).setContent(mathBui -> {
                        return mathBui.definitPar(
                                par -> par.callStatic(name.getText(), Object.class),
                                par -> (CallBuilder) index.build(par),
                                par -> (CallBuilder) value.build(par)
                        );
                    })._break();
                });

                return builder instanceof ModuleBuilder ? new ModuleBuilder(classBuilder.getClassAsm()) : classBuilder;
            } else if (builder instanceof BlockBuilder<?>){
                if (builder.getType().equals(ClinitDefinition.class)) {
                    return ((BlockBuilder) builder).callMethod(JPUtil.class, "setSerial", new Class[]{Object.class, Object.class, Object.class}, null).setContent(mathBui -> {
                        return mathBui.definitPar(
                                par -> par.callStatic(name.getText(), Object.class),
                                par -> (CallBuilder) index.build(par),
                                par -> (CallBuilder) value.build(par)
                        );
                    })._break();
                } else {
                    return ((BlockBuilder) builder).callMethod(JPUtil.class, "setSerial", new Class[]{Object.class, Object.class, Object.class}, null).setContent(mathBui -> {
                        return mathBui.definitPar(
                                par -> {
                                    try {
                                        return par.callLocal(name.getText());
                                    } catch (Exception e) {
                                        return par.callStatic(name.getText(), Object.class);
                                    }
                                },
                                par -> (CallBuilder) index.build(par),
                                par -> (CallBuilder) value.build(par)
                        );
                    })._break();
                }
            }

            throw new RuntimeException("no key");
        }

        if (builder instanceof ClassBuilder) {
            ClassBuilder classBuilder = ((ClassBuilder) builder).declareStaticVar(
                    this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class)
            ).setContent(setContent -> (CallBuilder) value.build(setContent));

            return builder instanceof ModuleBuilder ? new ModuleBuilder(classBuilder.getClassAsm()) : classBuilder;
        } else if (builder instanceof BlockBuilder<?>){
            if (builder.getType().equals(ClinitDefinition.class)) {
                return ((ClinitDefinition) builder).setVar(this.name.getText()).setContent(setContent -> {
                    return (CallBuilder<ClinitDefinition>) value.build(setContent);
                });
            } else {
                return ((BlockBuilder<?>) builder).setVar(this.name.getText()).setBlockContent(setContent -> {
                    Builder<?> ke = value.build(setContent);

                    if (ke instanceof CallBuilder<?>) {
                        return ((CallBuilder) ke)._break();
                    } else if (ke instanceof BlockBuilder<?>) {
                        return ((BlockBuilder) ke);
                    }

                    throw new RuntimeException("no key");
                });
            }
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Var{").newLine()
                .indent()
                .add("name=").add(name.getText()).newLine()
                .add("value:").indent();
        if (value != null) value.toString(indenter);

        indenter.newLine().unindent().add("index:").indent();
        if (index != null) index.toString(indenter);
        indenter.unindent().newLine()
                .unindent()
                .add("}");
    }
}
