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

import java.util.ArrayList;
import java.util.List;

public class SubCallStatement extends PyStatement {

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
