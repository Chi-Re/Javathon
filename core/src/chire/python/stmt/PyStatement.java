package chire.python.stmt;

import chire.asm.dynamic.builder.Builder;
import chire.python.util.SmartIndenter;

import java.util.*;

public abstract class PyStatement {
    public Builder<?> build(Builder<?> builder){
        return builder;
    }

    public void toString(SmartIndenter indenter){
    }

    @Override
    public String toString() {
        var str = new SmartIndenter("  ");
        toString(str);
        return str.toString();
    }
}
