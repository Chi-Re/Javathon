package chire.python.antlr;

import chire.asm.args.Args;
import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.ClinitDefinition;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import chire.python.asm.ModuleBuilder;
import chire.python.escape.ClassCall;
import chire.python.escape.JPUtil;
import chire.python.lib.PyList;
import chire.python.lib.base.PyObject;
import chire.python.util.SmartIndenter;
import chire.python.util.Test;
import chire.python.util.type.RemoveQuotes;
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;

public abstract class PyStatement {
    public abstract PyExecutor.PyInstruction build(PyAssembler builder);

    public Builder<?> build(Builder<?> builder){
        return builder;
    }

    public void toString(SmartIndenter indenter){
    }

    @Override
    public String toString() {
        var str = new SmartIndenter("  ");
        toString(str);
        return str.toString();
    }

    public static class BreakStatement extends PyStatement{
        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.BreakPy();
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Break");
        }
    }

    public static class ImportStatement extends PyStatement{
        private final String path;
        private String name;
        private final String packName;

        ImportStatement(String path, String packName){
            this.path = path;
            this.packName = packName;
            this.name = packName;
        }

        public void toName(String name) {
            this.name = name;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof ModuleBuilder) {
                return new ModuleBuilder(
                       ((ModuleBuilder) builder).declareStaticVar(this.name, Object.class).setContent(
                               argb -> argb.definitObj(
                                       Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                               )
                       ).getClassAsm()
                );
            } else if (builder instanceof ClassBuilder) {
                return ((ClassBuilder) builder).declareVar(this.name, Object.class).setContent(
                        argb -> argb.definitObj(
                                Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                        )
                );
            } else if (builder instanceof BlockBuilder<?>){
                return ((FunctionDefinition) builder).setVar(this.name).setContent(
                        argb -> argb.definitObj(
                                Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                        )
                );
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return null;
        }
    }

    public static class TypeStatement extends PyStatement{
        private final Token type;
        private final TypeStatement[] value;

        TypeStatement(Token type, TypeStatement... value) {
            this.type = type;
            this.value = value;
        }

        TypeStatement(Token type) {
            this(type, new TypeStatement[0]);
        }

        public String toType(){
            String path = type.getText().replaceAll("\"", "");

            if (path.indexOf("java:") == 0) {
                return path.replaceFirst("java:", "");
            } else {
                return Format.formatPack(Object.class);
            }
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            return super.build(builder);
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return null;
        }
    }

    /**定义变量*/
    public static class VarStatement extends PyStatement{
        public final Token name;

        public PyStatement value;

        public TypeStatement type;

        public VarStatement(Token name, PyStatement value, TypeStatement type) {
            if (name == null) throw new RuntimeException("name 不能为空");
            this.name = name;
            this.value = value;
            this.type = type;
        }

        VarStatement(Token name, PyStatement value) {
            this(name, value, null);
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof ModuleBuilder) {
//                if (builder.getClassAsm().getClassStaticVars().keySet().contains(name.getText())) {
//                    return new ModuleBuilder(((ModuleBuilder) builder).setContent(clineBui -> {
//                        return clineBui.setStaticVar(this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class))
//                                .setContent(staticeVar -> {
//                                    return (CallBuilder<ClinitDefinition>) value.build(staticeVar);
//                                });
//                    }).getClassAsm());
//                } else {
                    return new ModuleBuilder(
                            value.build(((ModuleBuilder) builder).declareStaticVar(this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class))).getClassAsm()
                    );
//                }
            } else if (builder instanceof ClassBuilder) {
                return value.build(((ClassBuilder) builder).declareVar(this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class)));
            } else if (builder instanceof BlockBuilder<?>){
                if (builder.getType().equals(ClinitDefinition.class)) {
                    return value.build(((ClinitDefinition) builder).setStaticVar(this.name.getText(), Object.class));
                } else {
                    return value.build(((BlockBuilder) builder).setVar(this.name.getText()));
                }
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.VarPy(name.getText(), value.build(builder));
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Var{").newLine()
                    .indent()
                    .add("name=").add(name.getText()).newLine()
                    .add("value:").indent();
            if (value != null) value.toString(indenter);
//            indenter.unindent()
//                    .newLine()
//                    .add("type:").indent();
//            type.toString();
            indenter.unindent().newLine()
                    .unindent()
                    .add("}");
        }
    }

    public static class ClassStatement extends PyStatement{
        public final Token name;
        public final PyStatement paternal;
        public final ArrayList<PyStatement> body;

        public ClassStatement(Token name, PyStatement paternal, ArrayList<PyStatement> body){
            this.name = name;
            this.paternal = paternal;
            this.body = body;
        }

        public ClassStatement(Token name, ArrayList<PyStatement> body){
            this(name,null,body);
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof ClassBuilder) {
                ClassBuilder cbuilder = ((ClassBuilder) builder).defineClass(this.name.getText(), Object.class);

                for (PyStatement statement : body) {
                    cbuilder = (ClassBuilder) statement.build(cbuilder);
                }

                if (cbuilder.getClassAsm().getOuter() != null) {
                    return new ClassBuilder(cbuilder.make().getClassAsm().closeInnerClass());
                } else {
                    return cbuilder;
                }

            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.PyInstruction> body = new ArrayList<>();

            for (PyStatement statement : this.body) {
                body.add(statement.build(builder));
            }

            return new PyExecutor.ClassPy(name.getText(),
                    paternal == null ? PyObject.class : paternal.build(builder).getClass(), body);
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Class{").newLine()
                    .indent()
                    .add("name=").add(name.getText()).newLine()
                    .add("paternal:").add(paternal == null ? "obj" : paternal.toString()).newLine()
                    .add("body:").indent();

            for (PyStatement statement : this.body) {
                statement.toString(indenter);
            }

            indenter.unindent().newLine().unindent()
                    .add("}");
        }
    }

    public static class FunStatement extends PyStatement{

        public final Token token;

        public final ArrayList<ArgStatement> args;

        public final ArrayList<PyStatement> statements;

        public FunStatement(Token token, ArrayList<ArgStatement> args, ArrayList<PyStatement> statements){
            this.args = args;
            this.token = token;
            this.statements = statements;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            Args args = new Args();

            if (builder instanceof ClassBuilder) {
//                builder = ((ClassBuilder) builder).defineVar(Opcodes.ACC_STATIC, token.getText()+"$"+this.args.get(0).token.getText(), Object.class).setContent(CallBuilder::callThis);
                FunctionDefinition fun;

                if (builder instanceof ModuleBuilder) {
                    for (ArgStatement arg : this.args) {
                        args.put(arg.token.getText(), arg.getType());
                    }

                    fun = ((ModuleBuilder) builder).defineFunction(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, token.getText(), args);
                } else {
                    for (int i = 1; i < this.args.size(); i++) {
                        args.put(this.args.get(i).token.getText(), this.args.get(i).getType());
                    }

                    fun = ((ClassBuilder) builder).defineFunction(Opcodes.ACC_PUBLIC, token.getText(), args);
                    fun = fun.setVar(this.args.get(0).token.getText()).setContent(BlockBuilder::callThis);
                }

                for (PyStatement statement : this.statements) {
                    Builder<?> bui = statement.build(fun);
                    if (bui instanceof CallBuilder<?>) {
                        fun = (FunctionDefinition) ((CallBuilder<?>) bui)._break();
                    } else {
                        fun = (FunctionDefinition) bui;
                    }
                }

                if (builder instanceof ModuleBuilder) {
                    return new ModuleBuilder(fun._return().getClassAsm());
                } else {
                    return fun._return();
                }
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.ArgPy> instArgs = new ArrayList<>();
            ArrayList<PyExecutor.PyInstruction> instStmts = new ArrayList<>();

            for (ArgStatement arg : this.args) {
                instArgs.add((PyExecutor.ArgPy) arg.build(builder));
            }

            for (PyStatement statement : this.statements) {
                instStmts.add(statement.build(builder));
            }

            return new PyExecutor.FunPy(token.getText(), instArgs, instStmts);
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().addLine("Fun{")
                    .indent()
                    .add("token=").addLine(token.getText())
                    .add("args=[").indent();

            for (ArgStatement arg : args) {
                arg.toString(indenter);
            }

            indenter.newLine().unindent().add("]").newLine()
                    .add("stmts=[")
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

    public static class WhileStatement extends PyStatement{

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
                            pd -> (ClinitDefinition) conditions.build(pd),
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
                        pd -> ((BlockBuilder<?>) conditions.build(pd)),
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
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.PyInstruction> instructions = new ArrayList<>();

            for (PyStatement statement : this.statements) {
                instructions.add(statement.build(builder));
            }

            return new PyExecutor.WhilePy(conditions.build(builder), instructions);
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

    public static class ForStatement extends PyStatement {
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
            if (builder instanceof ClassBuilder) {
                ClassBuilder cont = ((ClassBuilder) builder).setContent(content -> {
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
                                whiCont = whiCont.setVar(variable.getText())
                                        .setContent(vatBui -> {
                                            return vatBui.callLocal("for$"+variable.getText())
                                                    .callMethod(PyList.class, "next", new Class[]{}, Object.class)
                                                    .setContent(CallBuilder.ParameterBuilder::definitPar);
                                        }).out();

                                for (PyStatement statement : this.body) {
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
                    )
                    .out();
                });

                if (builder instanceof ModuleBuilder) {
                    return new ModuleBuilder(cont.getClassAsm());
                } else {
                    return cont;
                }
            }
//            else if (builder instanceof BlockBuilder<?>) {
//                return ((BlockBuilder) builder).setVar("for$"+variable.getText()).setContent(forVar -> {
//                    return forVar.callMethod(JPUtil.class, "iterator", new Class[]{Object.class}, Iterator.class).setContent(iterBui -> {
//                        return iterBui.definitPar(
//                                parbui -> (CallBuilder) iterable.build(parbui)
//                        );
//                    });
//                }).whileCall().setContent(
//                        pd ->
//                                pd.callLocal("for$"+variable.getText())
//                                        .callMethod(Iterator.class, "hasNext", new Class[]{}, boolean.class)
//                                        .setContent(CallBuilder.ParameterBuilder::definitPar)
//                                        ._break(),
//                        whiCont -> {
//                            whiCont = whiCont.setVar(variable.getText())
//                                    .setContent(vatBui -> {
//                                        return vatBui.callLocal("for$"+variable.getText())
//                                                .callMethod(Iterator.class, "next", new Class[]{}, Object.class)
//                                                .setContent(CallBuilder.ParameterBuilder::definitPar);
//                                    }).out();
//
//                            for (PyStatement statement : this.body) {
//                                Builder<?> bui = statement.build(whiCont);
//                                if (bui instanceof CallBuilder<?>) {
//                                    whiCont = (BlockBuilder<ClinitDefinition>) ((CallBuilder) bui)._break();
//                                } else {
//                                    whiCont = (BlockBuilder<ClinitDefinition>) bui;
//                                }
//                            }
//
//                            return whiCont;
//                        },
//                        Opcodes.IFEQ
//                );
//            }

            throw new RuntimeException("no key");
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return null;
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

    public static class PassStatement extends PyStatement{
        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.PassPy();
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("PassPy{}");
        }
    }

    public static class SubCallStatement extends PyStatement{

        public final PyStatement key;

        public final PyStatement call;

        public SubCallStatement(PyStatement var, PyStatement call) {
            this.key = var;
            this.call = call;
        }

        /**
         * @return CallBuilder or ModuleBuilder
         */
        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof CallBuilder<?>) {
                Builder outCall = createBlock(((CallBuilder<?>) builder)._break());

                return new CallBuilder<>(outCall.getClassAsm(), outCall.getType());
            }

            else if (builder instanceof BlockBuilder<?>) {
                return createBlock((BlockBuilder<?>) builder);
            } else if (builder instanceof ModuleBuilder){
                return new ModuleBuilder(((ModuleBuilder) builder).setContent(clinBui ->
                        createBlock(clinBui)._break()
                ).getClassAsm());
            } else if (builder instanceof ClassBuilder){
                return  ((ClassBuilder) builder).setContent(clinBui ->
                        createBlock(clinBui)._break()
                );
            } else if (builder instanceof ClassBuilder.StaticVarBuilder) {
                return ((ClassBuilder.StaticVarBuilder) builder).setContent(statBui -> {
                    return (CallBuilder<ClinitDefinition>) createBlock(statBui._break());
                });
            }

            throw new RuntimeException("no key");
        }

        public <T extends BlockBuilder<T>> CallBuilder<T> createBlock(BlockBuilder<T> builder) {
            CallBuilder<T> outBuilder;

            if (call instanceof VarCallStatement) {
                outBuilder = builder.callMethod(JPUtil.class, "callVar", new Class[]{Object.class, String.class}, Object.class)
                        .setContent(varBui ->  varBui.definitPar(
                                argBui -> (CallBuilder) key.build(argBui),
                                argBui -> argBui.definitObj(((VarCallStatement) call).name.getText())
                        ));
            } else if (call instanceof FunCallStatement) {
                List<AsmBudVisitor.AsmCallBuilder> callBuilders = new ArrayList<>();

                callBuilders.add(argBui -> (CallBuilder) key.build(((BlockBuilder)argBui._break())));
                callBuilders.add(argBui -> argBui.definitObj(((FunCallStatement) call).name.getText()));

                for (int i = 0; i < ((FunCallStatement) call).args.size(); i++) {
                    int finalI = i;
                    callBuilders.add(par -> (CallBuilder<?>) ((FunCallStatement) call).args.get(finalI).build(par));
                }

                outBuilder = builder.callMethod(JPUtil.class, "callMethod", new Class[]{Object.class, String.class, Object[].class}, Object.class)
                        .setContent(varBui ->  varBui.definitPar(
                                callBuilders.toArray(new AsmBudVisitor.AsmCallBuilder[0])
                        ));
            } else {
                throw new RuntimeException("no key");
            }

            return outBuilder;
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.SubCallPy(this.key.build(builder), this.call.build(builder));
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("SubCall{")
                    .indent()
                    .newLine().add("key:").indent();
            key.toString(indenter);

            indenter.unindent().newLine().add("call:").indent();

            call.toString(indenter);

            indenter.unindent().newLine().unindent().add("}");
        }
    }

    public static class SubSetStatement extends PyStatement{

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
//            if (call instanceof VarCallStatement) {
//                return builder.callMethod(JPUtil.class, "callVar", new Class[]{Object.class, String.class}, Object.class).setContent(funBui -> {
//                    return funBui.definitPar(
//                            parBui -> (CallBuilder) key.build(parBui),
//                            parBui -> parBui.definitObj(((VarCallStatement) call).name)
//                    );
//                })._break();
//            } else if (call instanceof FunCallStatement) {
//                return builder.callMethod(JPUtil.class, "callMethod", new Class[]{Object.class, String.class, Object[].class}, Object.class).setContent(funBui -> {
//                    return funBui.definitPar(
//                            parBui -> (CallBuilder) key.build(parBui),
//                            parBui -> parBui.definitObj(((FunCallStatement) call).name)
//                    );
//                })._break();
//            }

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
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.SubSetPy(key.build(builder), call.build(builder), var.build(builder));
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

    public static class ListStatement extends PyStatement{

        public final ArrayList<PyStatement> list;

        public ListStatement(ArrayList<PyStatement> list){
            this.list = list;
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.PyInstruction> instructions = new ArrayList<>();

            for (PyStatement statement : this.list) {
                instructions.add(statement.build(builder));
            }

            return new PyExecutor.ListPy(instructions);
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

    public static class NoneStatement extends PyStatement{
        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.NonePy();
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("null");
        }
    }

    public static class ReturnStatement extends PyStatement{
        public final PyStatement returnStmt;

        public ReturnStatement(PyStatement returnStmt){
            this.returnStmt = returnStmt;
        }

        public ReturnStatement(){
            this.returnStmt = new NoneStatement();
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.ReturnPy(returnStmt.build(builder));
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Return{")
                    .indent();
            returnStmt.toString(indenter);
            indenter.newLine()
                    .unindent()
                    .add("}");
        }
    }

    public static class ArgStatement extends PyStatement{

        public final Token token;

        public final TypeStatement type;

        public ArgStatement(Token token, TypeStatement type){
            this.token = token;
            this.type = type;
        }

        public String getType(){
            return type == null ? Format.formatPack(Object.class) : type.toType();
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.ArgPy(token.getText(), Object.class);
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Arg{").add(token.getText()).add("|").add(String.valueOf(type)).add("}");
        }
    }

    public static class IfStatement extends PyStatement {

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
                            condition -> (ClinitDefinition) conditions.build(condition),
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
                        condition -> (BlockBuilder) conditions.build(condition),
                        visitor -> {
                            for (PyStatement statement : this.statements) {
                                Builder<?> bui = statement.build(visitor);
                                if (bui instanceof CallBuilder<?>) {
                                    visitor = (BlockBuilder<?>) ((CallBuilder<?>) bui)._break();
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
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.PyInstruction> instructions = new ArrayList<>();

            for (PyStatement statement : this.statements) {
                instructions.add(statement.build(builder));
            }

            return new PyExecutor.IfPy(conditions.build(builder), instructions);
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
                return ((BlockBuilder<?>) builder).callMethod(JPUtil.class, "comparison", new Class[]{Object.class, Object.class, String.class}, boolean.class)
                        .setContent(budVis -> budVis.definitPar(
                                left -> (CallBuilder) this.left.build(left),
                                right -> (CallBuilder) this.right.build(right),
                                operator -> operator.definitObj(this.operator.getText())
                        ))._break();
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.JudgmentPy(left.build(builder), operator.getType(), right.build(builder));
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

    public static class NumberStatement<T> extends PyStatement{

        public final Token token;

        public final Class<T> type;

        public final boolean range;

        public NumberStatement(Token token, Class<T> type){
            this(true, token, type);
        }

        public NumberStatement(boolean range , Token token, Class<T> type){
            this.token = token;
            this.type = type;
            this.range = range;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof ClassBuilder.VarBuilder) {
                return ((ClassBuilder.VarBuilder) builder).setContent(builder1 -> builder1.definitObj(cast()));
            } else if (builder instanceof ClassBuilder.StaticVarBuilder) {
                return ((ClassBuilder.StaticVarBuilder) builder).setContent(builder1 -> builder1.definitObj(cast()));
            } else if (builder instanceof BlockBuilder.VarBuilder) {
                return ((BlockBuilder.VarBuilder<FunctionDefinition>) builder).setContent(builder1 -> builder1.definitObj(cast()));
            } else if (builder instanceof CallBuilder<?>) {
                return ((CallBuilder<?>) builder).definitObj(cast());
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.NumbePy(range, cast());
        }

        private Number cast() {
            if (Integer.class.equals(type)) {
                return Integer.valueOf(token.getText());
            } else if (Double.class.equals(type)) {
                return Double.valueOf(token.getText());
            } else if (Float.class.equals(type)) {
                return Float.valueOf(token.getText());
            }
            throw new RuntimeException("don't is num");
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Num{")
                    .add("key=").add(range ? "+" : "-").add(token.getText())
                    .add(", ")
                    .add("type=").add(String.valueOf(type))
                    .add("}");
        }
    }

    /**保存常数*/
    public static class ConstStatement<T> extends PyStatement{

        public final Token token;

        public final Class<T> type;

        public ConstStatement(Token token, Class<T> type){
            this.token = token;
            this.type = type;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof CallBuilder<?>) {
                if (type == String.class) {
                    return ((CallBuilder<?>) builder).definitObj(RemoveQuotes.removeQuotes(token.getText()));
                } else {
                    return ((CallBuilder<?>) builder).definitObj(cast());
                }
            } else if (builder instanceof BlockBuilder.VarBuilder<?>) {
                if (type == String.class) {
                    return ((BlockBuilder.VarBuilder<?>) builder).setContent(
                            bun -> bun.definitObj(RemoveQuotes.removeQuotes(token.getText()))
                    );
                } else {
                    return ((BlockBuilder.VarBuilder<?>) builder).setContent(
                            bun -> bun.definitObj(cast())
                    );
                }
            } else if (builder instanceof ClassBuilder.StaticVarBuilder) {
                if (type == String.class) {
                    return ((ClassBuilder.StaticVarBuilder) builder).setContent(
                            bun -> bun.definitObj(RemoveQuotes.removeQuotes(token.getText()))
                    );
                } else {
                    return ((ClassBuilder.StaticVarBuilder) builder).setContent(
                            bun -> bun.definitObj(cast())
                    );
                }
            } else {
                throw new RuntimeException("no key");
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            if (type == String.class) {
                return new PyExecutor.ConstPy(RemoveQuotes.removeQuotes(token.getText()));
            } else {
                return new PyExecutor.ConstPy(cast());
            }
        }

        private Object cast() {
            if (Object.class.equals(type)) {
                return null;
            }

            if (Boolean.class.equals(type)) {
                if (Objects.equals(token.getText(), "True")) return true;
                if (Objects.equals(token.getText(), "False")) return false;
            } else if (Integer.class.equals(type)){
                return Integer.parseInt(token.getText());
            } else if (Float.class.equals(type)) {
                return Float.parseFloat(token.getText());
            } else if (Double.class.equals(type)) {
                return Double.parseDouble(token.getText());
            }
            return null;
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("Cons{")
                    .add("key=").add(token.getText())
                    .add(", ")
                    .add("type=").add(type.toString())
                    .add("}");
        }
    }

    /**方法调用*/
    public static class FunCallStatement extends PyStatement{
        public final Token name;

        public ArrayList<PyStatement> args = new ArrayList<>();

        public FunCallStatement(Token name) {
            this.name = name;
        }

        public void setArg(ArrayList<PyStatement> args){
            this.args = args;
        }

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof CallBuilder<?>) {
//                for (PyStatement arg : args) {
//                    callBuilder = (CallBuilder<?>) arg.build(callBuilder);
//                }

                var callbl = makeContent(((CallBuilder<?>) builder)._break());

                return new CallBuilder<>(callbl.getClassAsm(), callbl.getType());
            } else if (builder instanceof ClassBuilder) {
                ClassBuilder classBuilder = ((ClassBuilder) builder).setContent(cilnBui -> {
                    if (!args.isEmpty()) {
                        cilnBui = makeContent(cilnBui);
                    } else {
                        cilnBui = makeContent(cilnBui);
                    }

                    return cilnBui.out();
                });

                if (builder instanceof ModuleBuilder) {
                    return new ModuleBuilder(classBuilder.getClassAsm());
                } else {
                    return classBuilder;
                }
            } else if (builder instanceof BlockBuilder<?>) {
                return makeContent((BlockBuilder<?>) builder);
            }
            else {
                throw new RuntimeException("no key");
            }
        }

        public <T extends BlockBuilder<T>> BlockBuilder<T> makeContent(BlockBuilder<T> builder) {
            List<AsmBudVisitor.AsmCallBuilder> callBuilders = new ArrayList<>();

            callBuilders.add(argBui -> argBui.definitObj(Type.getType("L"+argBui.getClassAsm().className+";")));
            callBuilders.add(argBui -> argBui.definitObj(name.getText()));

            if (!args.isEmpty()) {
                for (int i = 0; i < this.args.size(); i++) {
                    int finalI = i;
                    callBuilders.add(par -> (CallBuilder<?>) this.args.get(finalI).build(par));
                }

                return builder.callMethod(JPUtil.class, "callMethod", new Class[]{Object.class, String.class, Object[].class}, Object.class)
                        .setContent(varBui ->  varBui.definitPar(
                                callBuilders.toArray(new AsmBudVisitor.AsmCallBuilder[0])
                        ))._break();
            } else {
                return builder.callMethod(JPUtil.class, "callMethod", new Class[]{Object.class, String.class, Object[].class}, Object.class)
                        .setContent(varBui ->  varBui.definitPar(
                                callBuilders.toArray(new AsmBudVisitor.AsmCallBuilder[0])
                        ))._break();
            }
        }

//        public CallBuilder<?> makeContent(CallBuilder<?> builder) {
//            List<AsmBudVisitor.AsmCallBuilder> callBuilders = new ArrayList<>();
//
//            callBuilders.add(parBui -> parBui.definitObj(this.name.getText()));
//
//            if (!args.isEmpty()) {
//                for (int i = 0; i < args.size(); i++) {
//                    int finalI = i;
//                    callBuilders.add(par -> (CallBuilder<?>) args.get(finalI).build(par));
//                }
//            }
//
//            return builder.callMethod(JPUtil.class, "callMethod", new Class[]{Object.class, String.class, Object[].class}, Object.class)
//                    .setContent(varBui ->  varBui.definitPar(
//                            callBuilders.toArray(new AsmBudVisitor.AsmCallBuilder[0])
//                    ));
//
//            return builder.callClass(ClassCall.class, new Class[]{Object.class})
//                    .setContent(clasBui -> clasBui.definitObj(Type.getType("L"+builder.getClassAsm().className+";")))
//                    .callMethod(ClassCall.class, "callMethod", new Class[]{String.class, Object[].class}, ClassCall.class)
//                    .setContent(bu -> bu.definitPar(
//                            callBuilders.toArray(new AsmBudVisitor.AsmCallBuilder[0])
//                    ));
//        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            ArrayList<PyExecutor.PyInstruction> insts = new ArrayList<>();

            for (PyStatement arg : this.args) {
                insts.add(arg.build(builder));
            }

            return new PyExecutor.FunCallPy(name.getText(), insts);
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().addLine("CallFun{")
                    .indent()
                    .add("name=").addLine(name.getText())
                    .add("args=[");

            if (args.size() > 0) {
                indenter.indent();
                for (PyStatement arg : args) {
                    arg.toString(indenter);
                }
                indenter.newLine().unindent();
            }

            indenter.addLine("]")
                    .unindent()
                    .add("}");
        }
    }

    /**变量调用*/
    public static class VarCallStatement extends PyStatement{
        public final Token name;

        public VarCallStatement(Token name) {
            this.name = name;
        }

        /**
         * @return CallBuilder
         */
        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof CallBuilder<?>) {
                try {
                    return ((CallBuilder<?>) builder).callLocal(this.name.getText());
                } catch (Exception e) {
                    //TODO 这里需要支持获取本地类中的变量，理论上讲。
//                    if (builder.getType().equals(ClinitDefinition.class)) {
                        return ((CallBuilder<?>) builder).callStatic(this.name.getText(), Object.class);
//                    } else {
//                        try {
//                            return ((CallBuilder) builder)
//                        } catch (Exception e) {
//                            return ((CallBuilder<?>) builder).callStatic(this.name.getText(), Object.class);
//                        }
//                    }
                }
            } else if (builder instanceof BlockBuilder<?>) {
                try {
                    return ((BlockBuilder<?>) builder).callLocal(this.name.getText());
                } catch (Exception e) {
                    return ((BlockBuilder<?>) builder).callStatic(this.name.getText(), Object.class);
                }
            } else {
                return builder;
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            return new PyExecutor.VarCallPy(name.getText());
        }

        @Override
        public void toString(SmartIndenter indenter) {
            indenter.newLine().add("CallVar{").add("name=").add(name.getText()).add("}");
        }
    }

    public static class LogicalStatement extends PyStatement{
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
            } else if (builder instanceof ClassBuilder.StaticVarBuilder) {
                return ((ClassBuilder.StaticVarBuilder) builder).setContent(varBui -> {
                    return (varBui._break()).callMethod(JPUtil.class, "operation", new Class[]{Object.class, Object.class, String.class}, Object.class)
                            .setContent(logiBui -> logiBui.definitPar(
                                    logiPar -> (CallBuilder) left.build(logiPar),
                                    logiPar -> (CallBuilder) right.build(logiPar),
                                    logiPar -> logiPar.definitObj(operator != null ? operator.getText() : operatorStr)
                            ));
                });
            } else if (builder instanceof BlockBuilder.ClassVarBuilder) {
                return ((BlockBuilder.ClassVarBuilder) builder).setContent(varBui -> {
                    return varBui._break().callMethod(JPUtil.class, "operation", new Class[]{Object.class, Object.class, String.class}, Object.class)
                            .setContent(logiBui -> logiBui.definitPar(
                                    logiPar -> (CallBuilder) left.build(logiPar),
                                    logiPar -> (CallBuilder) right.build(logiPar),
                                    logiPar -> logiPar.definitObj(operator != null ? operator.getText() : operatorStr)
                            ));
                });
            }
            else {
                throw new RuntimeException("no key");
            }
        }

        @Override
        public PyExecutor.PyInstruction build(PyAssembler builder) {
            if (operator != null) {
                return new PyExecutor.LogicalPy(left.build(builder), operator.getText(), right.build(builder));
            } else {
                return new PyExecutor.LogicalPy(left.build(builder), operatorStr, right.build(builder));
            }
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
}
