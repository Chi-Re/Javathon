package chire.python;

import chire.antlr.Python3Lexer;
import chire.antlr.Python3Parser;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.antlr.PyParser;
import chire.python.antlr.PyStatement;
import chire.python.asm.PythonAsmBuddy;
import chire.python.util.SmartIndenter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PyCompiler {
    public static boolean debug = false;

    public static Map<String, byte[]> compile(String className, String pythonCode) {
        CharStream input = CharStreams.fromString(pythonCode);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        parser.file_input();  // 触发语法校验，若失败会抛出异常

        if (debug) for (Token token : tokens.getTokens()) {
            System.out.println(token);
        }

        ArrayList<PyStatement> statements = new PyParser(tokens).parse();

        ClassBuilder builder = new PythonAsmBuddy(className, Object.class).create();
        SmartIndenter indenter = new SmartIndenter("  ");

        for (PyStatement stmt : statements) {
            if (debug) stmt.toString(indenter);

            builder = (ClassBuilder) stmt.build(builder);
        }
        if (debug) System.out.println(indenter);

        return builder.make().save();
    }

    public static Map<String, byte[]> compile(File pyFile) throws IOException {
        String pythonCode = Files.readString(pyFile.toPath());
        String className = deriveClassName(pyFile);
        return compile(className, pythonCode);
    }

    public static Map<String, File> compileClassFile(File pyFile, File outputDir) throws IOException {
        Map<String, byte[]> bytecodes = compile(pyFile);
        Map<String, File> fileCodes = new HashMap<>();

        //TODO 这里需要更多的调整，但我还没想好。
        for (String kes : bytecodes.keySet()) {
            String classFilePath = kes.replace('.', '/') + ".class";
            Path targetPath = outputDir.toPath().resolve(classFilePath);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, bytecodes.get(kes));
            fileCodes.put(kes, targetPath.toFile());
        }

        return fileCodes;
    }

    private static String deriveClassName(File pyFile) {
        String name = pyFile.getName();
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx > 0) name = name.substring(0, dotIdx);
        return name;  // 无包名，可扩展
    }
}