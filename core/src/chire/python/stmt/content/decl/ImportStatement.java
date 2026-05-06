package chire.python.stmt.content.decl;

import chire.asm.dynamic.builder.BlockBuilder;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.asm.dynamic.definition.FunctionDefinition;
import chire.asm.util.Format;
import chire.python.asm.ModuleBuilder;
import chire.python.stmt.PyStatement;
import org.objectweb.asm.Type;

public class ImportStatement extends PyStatement {
    private final String path;
    private String name;
    private final String packName;

    public ImportStatement(String path, String packName){
        this.path = path;
        this.packName = packName;
        this.name = packName;
    }

    public void toName(String name) {
        this.name = name;
    }

    @Override
    public Builder<?> build(Builder<?> builder) {
        if (builder instanceof ClassBuilder) {
            ClassBuilder classBuilder = ((ClassBuilder) builder).declareStaticVar(this.name, Object.class).setContent(
                    argb -> argb.definitObj(
                            Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                    )
            );

            return builder instanceof ModuleBuilder ? new ModuleBuilder(classBuilder.getClassAsm()) : classBuilder;
        } else if (builder instanceof BlockBuilder<?>){
            return ((FunctionDefinition) builder).setVar(this.name).setContent(
                    argb -> argb.definitObj(
                            Type.getType(Format.formatStrPack(this.path+"."+this.packName)+";")
                    )
            );
        } else {
            return builder;
        }
    }
}
