package chire.python.stmt.content.expr;


import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.CallBuilder;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

/**变量调用*/
public class VarCallStatement extends PyStatement {
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
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("CallVar{").add("name=").add(name.getText()).add("}");
    }
}
