package chire.python.test;

import chire.python.PyCompiler;

import java.io.IOException;

public class Build {
    public static void main(String[] args) throws IOException {
        PyCompiler.main(new String[]{
                "C:\\Projects\\java\\JavaPythonInterpreter\\pycode",
                "C:\\Projects\\java\\JavaPythonInterpreter\\test\\build\\output.jar"
        });
    }
}
