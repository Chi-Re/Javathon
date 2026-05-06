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
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class IfStatement extends PyStatement {

    public final PyStatement conditions;

    public final ArrayList<PyStatement> statements;

    public final IfStatement elseStatement;

    public IfStatement(PyStatement conditions, ArrayList<PyStatement> statements){
        this(conditions, statements, null);
    }

    public IfStatement(PyStatement conditions, ArrayList<PyStatement> statements, IfStatement elseStatement){
        this.conditions = conditions;
        this.statements = statements;
        this.elseStatement = elseStatement;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof ClassBuilder) {
            ClassBuilder cont = ((ClassBuilder) builder).setContent(clinBui -> {
                return createElse(clinBui.ifCall().setContent(
                        condition -> condition.callMethod(JPUtil.class, "toBoolean", new Class[]{Object.class}, boolean.class).setContent(con -> {
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
                        visitor -> {
                            for (PyStatement statement : this.statements) {
                                Builder<?> bui = statement.build(visitor);
                                if (bui instanceof CallBuilder<?>) {
                                    visitor = (ClinitDefinition) ((CallBuilder<?>) bui)._break();
                                } else {
                                    visitor = (ClinitDefinition) bui;
                                }
                            }

                            return visitor;
                        },
                        Opcodes.IFEQ
                )).out();
            });

            if (builder instanceof ModuleBuilder) {
                return new ModuleBuilder(cont.getClassAsm());
            } else {
                return cont;
            }
        } if (builder instanceof BlockBuilder<?>) {
            return createElse(((BlockBuilder) builder).ifCall().setContent(
                    condition -> condition.callMethod(JPUtil.class, "toBoolean", new Class[]{Object.class}, boolean.class).setContent(con -> {
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
                    visitor -> {
                        for (PyStatement statement : this.statements) {
                            Builder<?> bui = statement.build(visitor);
                            if (bui instanceof CallBuilder<?>) {
                                visitor = ((CallBuilder<?>) bui)._break();
                            } else {
                                visitor = (BlockBuilder<?>) bui;
                            }
                        }

                        return visitor;
                    },
                    Opcodes.IFEQ
            )).out();
        }
        else {
            return super.build(builder);
        }
    }

    private <T extends BlockBuilder<T>> BlockBuilder<T> createElse(BlockBuilder<T>.IfElseBuilder elseBuilder) {
        if (elseStatement == null) {
            return elseBuilder.out();
        } else if (elseStatement.conditions == null) {
            return elseBuilder.toElse(toElseBui -> {
                for (PyStatement statement : elseStatement.statements) {
                    Builder<T> builder = (Builder<T>) statement.build(toElseBui);

                    if (builder instanceof CallBuilder<T>) {
                        return ((CallBuilder<T>) builder)._break();
                    } else {
                        return (BlockBuilder<T>) builder;
                    }
                }

                return toElseBui;
            });
        } else if (elseStatement.statements != null){
            return elseBuilder.toElse(toElseBui -> {
                return (BlockBuilder<T>) elseStatement.build(toElseBui);
            });
        } else {
            throw new RuntimeException("content is null!");
        }
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("If{").newLine()
                .indent()
                .add("cond:").indent();

        if (conditions != null) conditions.toString(indenter);

        indenter.unindent().newLine().indent();

        if (elseStatement != null) elseStatement.toString(indenter);

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

    public static class JudgmentStatement extends PyStatement{

        public final PyStatement left;

        public final Token operator;

        public final PyStatement right;

        public JudgmentStatement(PyStatement left, Token operator, PyStatement right){
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof BlockBuilder<?>) {
                return ((BlockBuilder<?>) builder).callMethod(JPUtil.class, "comparison", new Class[]{Object.class, Object.class, String.class}, Boolean.class)
                        .setContent(budVis -> budVis.definitPar(
                                left -> (CallBuilder) this.left.build(left),
                                right -> (CallBuilder) this.right.build(right),
                                operator -> operator.definitObj(this.operator.getText())
                        ))._break();
            } else if (builder instanceof CallBuilder<?>) {
                return ((CallBuilder<?>) builder)._break().callMethod(JPUtil.class, "comparison", new Class[]{Object.class, Object.class, String.class}, Boolean.class)
                        .setContent(budVis -> budVis.definitPar(
                                left -> (CallBuilder) this.left.build(left),
                                right -> (CallBuilder) this.right.build(right),
                                operator -> operator.definitObj(this.operator.getText())
                        ));
            }

            throw new RuntimeException("no key");
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().addLine("Judg{").indent();
            if (operator != null) {
                indenter.add("left:").indent();
                left.toString(indenter);
                indenter.unindent().newLine().add("operator='")
                        .indent().add(operator.getText()).add("'").unindent().newLine()
                        .add("indenter:").indent();
                right.toString(indenter);
                indenter.unindent();
            } else {
                indenter.newLine().indent();
                indenter.addLine("left:").indent();
                left.toString(indenter);
                indenter.unindent();
            }
            indenter.newLine().unindent().add("}");
        }
    }
}