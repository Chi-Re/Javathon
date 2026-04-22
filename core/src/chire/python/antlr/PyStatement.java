package chire.python.antlr;

import chire.asm.args.Args;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import chire.python.asm.ModuleBuilder;
import chire.python.escape.ClassCall;
import chire.python.escape.JPUtil;
import chire.python.lib.base.PyObject;
import chire.python.util.SmartIndenter;
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
                       ((ModuleBuilder) builder).declareStaticVar("JPClass_"+this.name, Class.class).setContent(
                               argb -> argb.definitObj(
                                       Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                               )
                       ).getClassAsm()
                );
            } else if (builder instanceof ClassBuilder) {
                return ((ClassBuilder) builder).declareVar(this.name, Class.class).setContent(
                        argb -> argb.definitObj(
                                Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                        )
                );
            } else if (builder instanceof FunctionDefinition){
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
                return new ModuleBuilder(
                        value.build(((ModuleBuilder) builder).declareStaticVar(this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class)))
                                .getClassAsm()
                );
            } else if (builder instanceof ClassBuilder) {
                return value.build(((ClassBuilder) builder).declareVar(this.name.getText(), this.type != null ? this.type.toType() : Format.formatPack(Object.class)));
            } else if (builder instanceof FunctionDefinition){
                return value.build(((FunctionDefinition) builder).setVar(this.name.getText()));
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
            value.toString(indenter);
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

            for (ArgStatement arg : this.args) {
                args.put(arg.token.getText(), arg.getType());
            }

            if (builder instanceof ClassBuilder) {
                FunctionDefinition fun;

                if (builder instanceof ModuleBuilder) {
                    fun = ((ModuleBuilder) builder).defineFunction(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, token.getText(), args);
                } else {
                    fun = ((ClassBuilder) builder).defineFunction(Opcodes.ACC_PUBLIC, token.getText(), args);
                }

                for (PyStatement statement : this.statements) {
                    Builder<?> bui = statement.build(fun);
                    if (bui instanceof CallBuilder<?>) {
                        fun = (FunctionDefinition) ((CallBuilder<?>) bui).out();
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

        @Override
        public Builder<?> build(Builder<?> builder) {
            if (builder instanceof BlockBuilder<?>) {
                if (key instanceof VarCallStatement) {
                    builder = ((BlockBuilder<?>) builder).callClass(ClassCall.class, new Class[]{Object.class})
                            .setContent(bu ->
                                    bu.callStatic("JPClass_"+((VarCallStatement) key).name.getText(), Class.class)
                            );
                } else if (key instanceof SubCallStatement) {
                    builder = key.build(builder);
                } else {
                    throw new RuntimeException("no key");
                }

                if (!(builder instanceof CallBuilder<?>)) throw new RuntimeException("no key");

                if (call instanceof VarCallStatement) {
                    builder = ((CallBuilder<?>) builder).callMethod(ClassCall.class, "call", new Class[]{String.class}, ClassCall.class)
                            .setContent(bu ->
                                    bu.definitObj(((VarCallStatement) call).name.getText())
                            );
                } else if (call instanceof FunCallStatement) {
                    builder = ((CallBuilder<?>) builder).callMethod(ClassCall.class, "callMethod", new Class[]{String.class, Object[].class}, ClassCall.class)
                            .setContent(bu -> bu.definitPar(
                                    parBui -> parBui.definitObj(((FunCallStatement) call).name.getText()),
                                    parBui -> ((FunCallStatement) call).build(parBui)
                            ));
                }

                return builder;
            } else if (builder instanceof CallBuilder<?>){

            }

            throw new RuntimeException("no key");
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

        public IfStatement(PyStatement conditions, ArrayList<PyStatement> statements){
            this.conditions = conditions;
            this.statements = statements;
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
        public CallBuilder<?> build(Builder<?> builder) {
            if (builder instanceof CallBuilder<?> callBuilder) {
                for (PyStatement arg : args) {
                    callBuilder = (CallBuilder<?>) arg.build(callBuilder);
                }

                return callBuilder;
            } else {
                throw new RuntimeException("no key");
            }
        }

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
