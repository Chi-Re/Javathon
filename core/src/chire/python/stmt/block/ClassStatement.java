package chire.python.stmt.block;

import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.asm.ModuleBuilder;
import chire.python.stmt.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ClassStatement extends PyStatement {
    public final Token name;
    public final PyStatement paternal;
    public final ArrayList<PyStatement> body;

    public ClassStatement(Token name, PyStatement paternal, ArrayList<PyStatement> body){
        this.name = name;
        this.paternal = paternal;
        this.body = body;
    }

    public ClassStatement(Token name, ArrayList<PyStatement> body){
        this(name,null,body);
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof ClassBuilder) {
            ClassBuilder cbuilder = ((ClassBuilder) builder).defineClass(this.name.getText(), Object.class);

            for (PyStatement statement : body) {
                cbuilder = (ClassBuilder) statement.build(cbuilder);
            }

            if (cbuilder.getClassAsm().getOuter() != null) {
                if (builder instanceof ModuleBuilder) {
                    return new ModuleBuilder(cbuilder.make().getClassAsm().closeInnerClass());
                } else {
                    return new ClassBuilder(cbuilder.make().getClassAsm().closeInnerClass());
                }
            } else {
                return cbuilder;
            }

        } else {
            return builder;
        }
    }

    @Override
    public void toString(SmartIndenter indenter) {
        indenter.newLine().add("Class{").newLine()
                .indent()
                .add("name=").add(name.getText()).newLine()
                .add("paternal:").add(paternal == null ? "obj" : paternal.toString()).newLine()
                .add("body:").indent();

        for (PyStatement statement : this.body) {
            statement.toString(indenter);
        }

        indenter.unindent().newLine().unindent()
                .add("}");
    }
}
