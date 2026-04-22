package chire.python;

import chire.asm.AsmBuddy;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PyInterpreter {
    public static void main(String[] args) throws Exception {
        //TODO
        // 38 bool
        // 4 int do
        // 3 str
        // 56 *
        // 72 -
        // 71 +
        // 73 /
        String pythonCode = """
                class Test:
                    def __init__(self):
                        pass
                
                    def test(self):
                        print("test")
                
                    def func(self):
                        print("func")
                """;

        PyCompiler.debug = true;

        try (FileOutputStream fos = new FileOutputStream("cache/test/ClassPyTest.class")) {
            fos.write(PyCompiler.compile("ClassPyTest", pythonCode));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        PyInterpreter.runScript("C:\\Projects\\java\\JavaPythonInterpreter\\pycode\\main.py");
    }

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
        byte[] bytecode = PyCompiler.compile(pyFile);
        String className = deriveClassNameFromFile(pyFile);
        Class<?> clazz = loadClass(className, bytecode);

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
