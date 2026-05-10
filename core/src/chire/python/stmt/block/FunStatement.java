package chire.python.stmt.block;

import chire.asm.args.Args;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import chire.python.asm.ModuleBuilder;
import chire.python.stmt.PyStatement;
import chire.python.stmt.content.decl.ArgStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

public class FunStatement extends PyStatement {

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
            FunctionDefinition fun;

            for (ArgStatement arg : this.args) {
                args.put(arg.token.getText(), Object.class);
            }

            fun = ((ClassBuilder) builder).defineFunction(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, token.getText(), args, Object.class);

            for (PyStatement statement : this.statements) {
                Builder<?> bui = statement.build(fun);
                if (bui instanceof CallBuilder<?>) {
                    fun = (FunctionDefinition) ((CallBuilder<?>) bui)._break();
                } else {
                    fun = (FunctionDefinition) bui;
                }
            }

            if (builder instanceof ModuleBuilder) {
                return new ModuleBuilder(fun._return(re -> re.definitObj(null)._break())._back().getClassAsm());
            } else {
                return fun._return(re -> re.definitObj(null)._break())._back();
            }
        }

        throw new RuntimeException("no key");
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

    public static class TypeStatement extends PyStatement{
        private final Token type;
        private final TypeStatement[] value;

        TypeStatement(Token type, TypeStatement... value) {
            this.type = type;
            this.value = value;
        }

        public TypeStatement(Token type) {
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
    }
}
