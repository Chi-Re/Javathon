package chire.python;

import chire.python.lib.PyConfig;
import chire.python.util.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyInterpreter {

    /**
     * @param args [directory] [classPath]
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) throw new RuntimeException("no key");

        PyInterpreter interpreter = new PyInterpreter();
        interpreter.loadFiles(new File(args[0]));

        if (args.length == 2) {
            interpreter.execClass(args[1]);
        }
    }

    private static final ByteArrayClassLoader dynamicLoader;

    static {
        //TODO 之后采用沙箱设计，但未解决JPUtil.forClass的问题。
        dynamicLoader = new ByteArrayClassLoader(Thread.currentThread().getContextClassLoader());
        PyConfig.loader = () -> dynamicLoader;
    }

    public PyInterpreter() {
    }

    public void loadFiles(File path) throws IOException {
        if (!path.isDirectory() || !path.exists()) throw new IOException("The loading file type is wrong or non-existent!");

        DirectoryWalker.walk(path, file -> {
            if (file.getName().endsWith(".py")) {
                String clazzPath = file.toPath().toString().replace(path.toPath()+"\\", "");
                clazzPath = clazzPath.substring(0, clazzPath.length()-3).replace("\\", ".");

                Map<String, byte[]> clamap = PyCompiler.compile(clazzPath, Files.readString(file.toPath()));

                for (String cla : clamap.keySet()) {
                    loadClass(cla.replaceAll("/", "."), clamap.get(cla));
                }
            }
        });
    }

    public void loadFile(File file, String name) throws IOException {
        if (file.getName().endsWith(".py")) {
            Map<String, byte[]> clamap = PyCompiler.compile(name, Files.readString(file.toPath()));

            for (String cla : clamap.keySet()) {
                loadClass(cla.replaceAll("/", "."), clamap.get(cla));
            }
        }
    }

    public Class<?> execClass(String name) {
        return forClass(name);
    }

    public Class<?> execFile(File file) {
        try {
            String name = file.getName();
            if (name.contains(".")) name = name.split("\\.")[0];
            loadFile(file, name);
            return execClass(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> forClass(String name) {
        try {
            return Class.forName(name, true, dynamicLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> loadClass(String name, byte[] bytecode) {
        return dynamicLoader.defineClass(name, bytecode);
    }

    private static class ByteArrayClassLoader extends ClassLoader {
        public ByteArrayClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
