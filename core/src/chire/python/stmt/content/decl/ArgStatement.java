package chire.python.stmt.content.decl;

import chire.asm.util.Format;
import chire.python.stmt.PyStatement;
import chire.python.stmt.block.FunStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

public class ArgStatement extends PyStatement {

    public final Token token;

    public final FunStatement.TypeStatement type;
    public final PyStatement body;

    public ArgStatement(Token token, FunStatement.TypeStatement type){
        this(token, type, null);
    }

    public ArgStatement(Token token, FunStatement.TypeStatement type, PyStatement body){
        this.token = token;
        this.type = type;
        this.body = body;
    }

    public String getType(){
        return type == null ? Format.formatPack(Object.class) : type.toType();
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Arg{").add(token.getText()).add("|").add(String.valueOf(type)).add("}");
    }
}
