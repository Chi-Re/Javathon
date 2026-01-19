package chire.python.antlr;

import chire.asm.ClassAsm;

public class PyAssembler extends ClassAsm {
    public PyAssembler() {
        super();

        defineClass("TestPyClass", Object.class);
    }
}
