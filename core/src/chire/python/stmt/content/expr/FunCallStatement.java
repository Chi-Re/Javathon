package chire.python.stmt.content.expr;

import chire.asm.dynamic.AsmBudVisitor;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.asm.ModuleBuilder;
import chire.python.lib.escape.JPUtil;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**方法调用*/
public class FunCallStatement extends PyStatement {
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
            var callbl = makeContent(((CallBuilder<?>) builder)._break());

            return new CallBuilder<>(callbl.getClassAsm(), callbl.getType());
        } else if (builder instanceof ClassBuilder) {
            ClassBuilder classBuilder = ((ClassBuilder) builder).setContent(cilnBui -> {
                return makeContent(cilnBui).out();
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
