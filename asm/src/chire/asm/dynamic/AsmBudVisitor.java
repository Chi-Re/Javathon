package chire.asm.dynamic;

import chire.asm.ClassAsm;
import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.CallBuilder;

public interface AsmBudVisitor<T extends BlockBuilder<T>> {
    CallBuilder<T> visit(CallBuilder.ParameterBuilder<T> builder);

    interface AsmCallBuilder<T extends BlockBuilder<T>> {
        CallBuilder<T> visit(CallBuilder<T> builder);
    }

    interface AsmBlockBuilder<T extends BlockBuilder<T>> {
        T visit(BlockBuilder<T> builder);
    }

    interface IfBuilder<T extends BlockBuilder<T>> {
        BlockBuilder<T> visit(BlockBuilder<T> builder);
    }

    interface SetCallBuilder<T extends BlockBuilder<T>> {
        CallBuilder<T> visit(BlockBuilder<T> builder);
    }

    interface SetBlockBuilder<T extends BlockBuilder<T>> {
        BlockBuilder<T> visit(BlockBuilder<T> builder);
    }
}
