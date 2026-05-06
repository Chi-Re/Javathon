package chire.python.stmt.content.control;

import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;

public class BreakStatement extends PyStatement {
    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Break");
    }
}
