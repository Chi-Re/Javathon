package chire.python.test;

import chire.py.main;
import chire.python.PyCompiler;

import java.io.IOException;

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
