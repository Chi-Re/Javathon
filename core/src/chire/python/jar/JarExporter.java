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

            // 添加默认的 MANIFEST.MF（可选）
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            JarEntry manifestEntry = new JarEntry(JarFile.MANIFEST_NAME);
            jos.putNextEntry(manifestEntry);
            manifest.write(jos);
            jos.closeEntry();
        }
    }
}
