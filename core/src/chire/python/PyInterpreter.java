package chire.python;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyInterpreter {
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static volatile ByteArrayClassLoader dynamicLoader;

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
    public static Object runScript(String scriptPath, String... args) throws Exception {
        File pyFile = new File(scriptPath);
        Map<String, byte[]> bytecode = PyCompiler.compile(pyFile);
        String className = deriveClassNameFromFile(pyFile);

        //TODO 这里还需要调整，暂时如此
        Class<?> clazz = loadClass(className, bytecode.get(bytecode.keySet().toArray(new String[0])[0]));

        Method mainMethod = clazz.getMethod("main", String[].class);
        return mainMethod.invoke(null, (Object) args);
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
