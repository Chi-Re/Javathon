package chire.python.jar;

import java.io.*;
import java.util.Map;
import java.util.jar.*;

public class JarExporter {
    public static void saveJar(Map<String, byte[]> classBytes, String jarFilePath) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFilePath))) {
            for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
                String className = entry.getKey();
                byte[] bytecode = entry.getValue();

                String entryName = className.replace('.', '/') + ".class";
                JarEntry jarEntry = new JarEntry(entryName);
                jos.putNextEntry(jarEntry);
                jos.write(bytecode);
                jos.closeEntry();
            }
        }
    }

    public static void saveClass(Map<String, byte[]> classBytes, String outPath) throws IOException {
        for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
            String className = entry.getKey();
            byte[] bytecode = entry.getValue();

            String entryName = className.replace('.', '/') + ".class";

            File file = new File(outPath + "/" + entryName);

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                    new FileOutputStream(file))) {
                bufferedOutputStream.write(bytecode);
            }
        }
    }
}
