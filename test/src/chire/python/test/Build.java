package chire.python.test;

import chire.python.PyCompiler;

import java.io.IOException;

public class Build {
    public static void main(String[] args) throws IOException {
//        PyCompiler.debug = true;

        PyCompiler.main(new String[]{
                ".\\test\\pycode",
                ".\\test\\build\\output.jar"
        });
    }
}
