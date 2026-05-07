package chire.python;

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
     * @param args [directory]
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) throw new RuntimeException("no key");

        File path = new File(args[0]);

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

//        System.out.println(CLASS_CACHE.get("chire.py.main").getMethod("main", Object.class).invoke(null, "ssssssss"));
    }

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final ByteArrayClassLoader dynamicLoader;

    static {
        dynamicLoader = new ByteArrayClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 直接运行一个Python脚本（无需手动编译为class文件）。
     *
     * @param scriptPath .py文件路径
     * @param args       传递给Python脚本中main函数的参数（若有）
     * @return 脚本执行结果（若脚本无返回值则返回null）
     * @throws Exception 编译或执行异常
     */
    public static Class<?> runScript(String scriptPath, String... args) throws Exception {
        File pyFile = new File(scriptPath);
        Map<String, byte[]> bytecode = PyCompiler.compile(pyFile);
        String className = deriveClassNameFromFile(pyFile);

        return loadClass(className, bytecode.get(bytecode.keySet().toArray(new String[0])[0]));
    }

    private static Class<?> loadClass(String name, byte[] bytecode) {
        return CLASS_CACHE.computeIfAbsent(name, n -> dynamicLoader.defineClass(n, bytecode));
    }

    private static String deriveClassNameFromFile(File pyFile) {
        String name = pyFile.getName();
        return name.substring(0, name.lastIndexOf('.'));
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
