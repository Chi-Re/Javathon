package chire.python.test;

import chire.py.main;
import chire.python.PyCompiler;
import chire.python.lib.builtins.PyDict;
import chire.python.lib.builtins.PyObject;
import chire.python.test.asm.Test2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        System.out.println(new main());
    }

    public static String valueOf(String pack, String path) {
        int current = 0;

        while (path.indexOf(".") == 0) {
            path = path.substring(1);

            current++;
        }

        return String.valueOf(current);
    }
}
