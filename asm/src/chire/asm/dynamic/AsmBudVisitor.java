package chire.asm.dynamic;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.CallBuilder;

public interface AsmBudVisitor<T> {
    CallBuilder<T> visit(CallBuilder.ParameterBuilder<T> builder);
}
