package chire.python;

import chire.antlr.Python3Lexer;
import chire.antlr.Python3Parser;
import chire.asm.dynamic.builder.ClassBuilder;
import chire.python.antlr.PyParser;
import chire.python.antlr.PyStatement;
import chire.python.asm.PythonAsmBuddy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PyCompiler {

    public static byte[] compile(String className, String pythonCode) {
        CharStream input = CharStreams.fromString(pythonCode);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        parser.file_input();  // 触发语法校验，若失败会抛出异常

        ArrayList<PyStatement> statements = new PyParser(tokens).parse();

        ClassBuilder builder = new PythonAsmBuddy(className, Object.class).create();
        for (PyStatement stmt : statements) {
            builder = (ClassBuilder) stmt.build(builder);
        }

        // 4. 生成字节数组
        return builder.make().save();
    }

    public static byte[] compile(File pyFile) throws IOException {
        String pythonCode = Files.readString(pyFile.toPath());
        String className = deriveClassName(pyFile);
        return compile(className, pythonCode);
    }

    public static File compileToClassFile(File pyFile, File outputDir) throws IOException {
        byte[] bytecode = compile(pyFile);
        String className = deriveClassName(pyFile);
        String classFilePath = className.replace('.', '/') + ".class";
        Path targetPath = outputDir.toPath().resolve(classFilePath);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, bytecode);
        return targetPath.toFile();
    }

    private static String deriveClassName(File pyFile) {
        String name = pyFile.getName();
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx > 0) name = name.substring(0, dotIdx);
        return name;  // 无包名，可扩展
    }

    /**
     * 命令行入口：java chire.python.PyCompiler <input.py> [output_dir]
     * 若不指定输出目录，默认输出到当前目录下的 "generated_classes"
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("用法: PyCompiler <python_file.py> [output_directory]");
            System.exit(1);
        }
        File inputFile = new File(args[0]);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("错误: 找不到Python文件 " + inputFile.getAbsolutePath());
            System.exit(1);
        }
        File outputDir = args.length >= 2 ? new File(args[1]) : new File("generated_classes");
        try {
            File classFile = compileToClassFile(inputFile, outputDir);
            System.out.println("编译成功: " + classFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("编译失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}