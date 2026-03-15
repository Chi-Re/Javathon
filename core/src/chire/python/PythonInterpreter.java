package chire.python;

import chire.antlr.Python3Lexer;
import chire.antlr.Python3Parser;
import chire.asm.AsmBuddy;
import chire.asm.dynamic.builder.Builder;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.antlr.PyExecutor;
import chire.python.antlr.PyParser;
import chire.python.antlr.PyStatement;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class PythonInterpreter {
    public static void main(String[] args) {
        //TODO
        // 38 bool
        // 4 int do
        // 3 str
        // 56 *
        // 72 -
        // 71 +
        // 73 /
        String pythonCode = """
                a: int = 1
                b = 3
                
                n.m.f()
                
                def main():
                    g = 1
                
                def fun(te):
                    c = 4
                """;

        // 创建词法分析器和语法分析器
        CharStream input = CharStreams.fromString(pythonCode);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        parser.file_input();

        for (Token token : tokens.getTokens()) {
            System.out.println(token);
        }

        ArrayList<PyStatement> statements = new PyParser(tokens).parse();

        Builder<?> builder = new AsmBuddy("ClassPyTest", Object.class).create();

        SmartIndenter indenter = new SmartIndenter("  ");
        for (PyStatement statement : statements) {
            statement.toString(indenter);

            builder = statement.build(builder);
        }
        System.out.println(indenter.toString());

        if (builder instanceof ClassBuilder) {
            try (FileOutputStream fos = new FileOutputStream("cache/test/ClassPyTest.class")) {
                fos.write(((ClassBuilder) builder).make().save());
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        } else {
            System.out.println(builder.getClass());
        }
    }

    public PythonInterpreter() {
//        set result 0
//        wait 0.5
//        op add result a b
    }
}
