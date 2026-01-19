package chire.asm.dynamic;

import chire.asm.ClassAsm;
import org.objectweb.asm.MethodVisitor;

public interface VarVisitor {
    void init(ClassAsm mv);
}
