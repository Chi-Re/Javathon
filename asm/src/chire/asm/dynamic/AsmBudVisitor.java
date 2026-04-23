package chire.asm.dynamic;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.CallBuilder;

public interface AsmBudVisitor<T> {
    CallBuilder<T> visit(CallBuilder.ParameterBuilder<T> builder);

    interface AsmCallBuilder<T> {
        CallBuilder<T> visit(CallBuilder<T> builder);
    }

    interface IfBuilder<T> {
        BlockBuilder<T> visit(T builder);
    }
}
