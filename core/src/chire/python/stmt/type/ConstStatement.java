package chire.python.stmt.type;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import chire.python.util.type.RemoveQuotes;
import org.antlr.v4.runtime.Token;

import java.util.Objects;

public class ConstStatement<T> extends PyStatement {

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
        } else if (builder instanceof BlockBuilder<?>) {
            if (type == String.class) {
                return ((BlockBuilder<?>) builder).definitObj(RemoveQuotes.removeQuotes(token.getText()));
            } else {
                return ((BlockBuilder<?>) builder).definitObj(cast());
            }
        }

        throw new RuntimeException("no key");
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
