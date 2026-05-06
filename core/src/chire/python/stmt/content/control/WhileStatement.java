package chire.python.stmt.content.control;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.python.asm.ModuleBuilder;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class WhileStatement extends PyStatement {

    public final PyStatement conditions;

    public final ArrayList<PyStatement> statements;

    public WhileStatement(PyStatement conditions, ArrayList<PyStatement> statements){
        this.conditions = conditions;
        this.statements = statements;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof ClassBuilder) {
            ClassBuilder cont = ((ClassBuilder) builder).setContent(content -> {
                return content.whileCall().setContent(
                        pd -> pd.callMethod(JPUtil.class, "toBoolean", new Class[]{Object.class}, boolean.class).setContent(con -> {
                            return con.definitPar(par -> {
                                var bui = this.conditions.build(par);

                                if (bui instanceof CallBuilder<?>) {
                                    return (CallBuilder<?>) bui;
                                } else if (bui instanceof BlockBuilder<?>) {
                                    return ((BlockBuilder<?>) bui).toCall();
                                }

                                throw new RuntimeException("no key");
                            });
                        })._break(),
                        whiCont -> {
                            for (PyStatement statement : this.statements) {
                                Builder<?> bui = statement.build(whiCont);
                                if (bui instanceof CallBuilder<?>) {
                                    whiCont = (BlockBuilder<ClinitDefinition>) ((CallBuilder) bui)._break();
                                } else {
                                    whiCont = (BlockBuilder<ClinitDefinition>) bui;
                                }
                            }

                            return whiCont;
                        },
                        Opcodes.IFEQ
                ).out();
            });

            if (builder instanceof ModuleBuilder) {
                return new ModuleBuilder(cont.getClassAsm());
            } else {
                return cont;
            }
        } else if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder)builder).whileCall().setContent(
                    pd -> pd.callMethod(JPUtil.class, "toBoolean", new Class[]{Object.class}, boolean.class).setContent(con -> {
                        return con.definitPar(par -> {
                            var bui = this.conditions.build(par);

                            if (bui instanceof CallBuilder<?>) {
                                return (CallBuilder<?>) bui;
                            } else if (bui instanceof BlockBuilder<?>) {
                                return ((BlockBuilder<?>) bui).toCall();
                            }

                            throw new RuntimeException("no key");
                        });
                    })._break(),
                    whiCont -> {
                        for (PyStatement statement : this.statements) {
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
            );
        }

        throw new RuntimeException("no key");
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("While{").newLine()
                .indent()
                .add("cond:").indent();
        conditions.toString(indenter);

        indenter.unindent().newLine()
                .add("stmt=[")
                .indent();
        for (PyStatement statement : statements) {
            statement.toString(indenter);
        }

        indenter.newLine()
                .unindent()
                .addLine("]")
                .unindent()
                .add("}");
    }
}