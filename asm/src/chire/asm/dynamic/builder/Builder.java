package chire.asm.dynamic.builder;

import chire.asm.ClassAsm;

public abstract class Builder {
    protected final ClassAsm classAsm;

    public Builder(ClassAsm classAsm) {
        this.classAsm = classAsm;
    }
}
