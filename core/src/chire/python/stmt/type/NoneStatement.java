package chire.python.stmt.type;

import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;

public class NoneStatement extends PyStatement {
    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("null");
    }
}
