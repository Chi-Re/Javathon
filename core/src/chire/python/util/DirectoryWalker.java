package chire.python.util;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**独立的文件扫描器*/
public class DirectoryWalker {
    public static void walk(File directory, FileHandler handler) throws IOException {
        if (directory == null || !directory.exists()) {
            throw new IllegalArgumentException("目录不存在: " + directory);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("路径不是目录: " + directory);
        }

        File[] children = directory.listFiles();
        if (children == null) {
            return;
        }

        for (File child : children) {
            if (child.isDirectory()) {
                walk(child, handler);
            } else {
                handler.handle(child);
            }
        }
    }

    @FunctionalInterface
    public interface FileHandler {
        void handle(File file) throws IOException;
    }
}
