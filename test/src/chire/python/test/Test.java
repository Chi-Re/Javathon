package chire.python.test;

import chire.py.main;
import chire.python.PyCompiler;

import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        PyCompiler.main(new String[]{
                "C:\\Projects\\java\\JavaPythonInterpreter\\pycode\\src",
                "C:\\Projects\\java\\JavaPythonInterpreter\\pycode\\output.jar"
        });

//        new main();
    }
}
