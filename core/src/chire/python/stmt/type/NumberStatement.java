package chire.python.stmt.type;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

public class NumberStatement<T> extends PyStatement {

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
        if (builder instanceof BlockBuilder<?>) {
            return ((BlockBuilder) builder).definitObj(cast());
        } else if (builder instanceof CallBuilder<?>) {
            return ((CallBuilder<?>) builder).definitObj(cast());
        }

        throw new RuntimeException("no key");
    }

    private Number cast() {
        if (Integer.class.equals(type)) {
            return Integer.valueOf(token.getText()) * (range ? 1 : -1);
        } else if (Double.class.equals(type)) {
            return Double.valueOf(token.getText()) * (range ? 1 : -1);
        } else if (Float.class.equals(type)) {
            return Float.valueOf(token.getText()) * (range ? 1 : -1);
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
