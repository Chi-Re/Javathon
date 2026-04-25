package chire.asm.util;

import chire.asm.args.Args;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class Format {

    public static final Map<Class<?>, String> keywords = new HashMap<Class<?>, String>(){{
        put(int.class, "I");
        put(boolean.class, "Z");
        put(void.class, "V");
    }};

    public static String formatPack(Class<?> clazz, boolean prefix) {
        String str = clazz.getName().replace(".", "/").replace(";", "");

        if (!prefix) return str;

        if ((str.startsWith("[") && str.indexOf("L") == 1) || str.startsWith("L")) {
            return str;
        } else {
            return "L"+str;
        }
    }

    public static String formatPack(Class<?> clazz) {
        return formatPack(clazz, true);
    }

    public static String formatPacks(Class<?>[] classes) {
        StringBuilder bs = new StringBuilder();
        bs.append("(");

        for (int i = 0; i < classes.length; i++) {
            bs.append(formatPack(classes[i])).append(";");
        }
        bs.append(")");

        return bs.toString();
    }

    public static String formatArgs(Args args, Class<?> returnType) {
        return args+(returnType==null?"V":(Format.formatPack(returnType)+";"));
    }

    public static String formatParameter(Class<?>[] parameterTypes, Class<?> returnType){
        if (keywords.containsKey(returnType)) {
            return formatPacks(parameterTypes) + keywords.get(returnType);
        } else {
            return formatPacks(parameterTypes) + (returnType == null ? "V" : (Format.formatPack(returnType) + ";"));
        }
    }

    public static String formatStrPack(String path, boolean arr) {
        String str = formatStrType(path);

        if (arr) {
            return "[L"+str;
        } else {
            return "L"+str;
        }
    }

    public static String formatStrPack(String path) {
        return formatStrPack(path, false);
    }

    public static String formatStrPacks(String[] classes) {
        StringBuilder bs = new StringBuilder();
        bs.append("(");

        for (String aClass : classes) {
            bs.append(aClass).append(";");
        }
        bs.append(")");

        return bs.toString();
    }

    public static String formatStrParameter(String[] parameterTypes, String returnType){
        return formatStrPacks(parameterTypes)+(returnType==null?"V":returnType+";");
    }

    public static String formatStrType(Class<?> clazz) {
        String path = clazz.getName();

        return formatStrType(path);
    }

    public static String formatStrType(String path) {
        String str = path.replace(".", "/").replace(";", "");

        if (path.indexOf("L") == 0){
            str = str.replaceFirst("L", "");
        } else if (path.indexOf("L") == 1 && path.indexOf("[") == 0) {
            str = str.replaceFirst("\\[", "").replaceFirst("L", "");
        }

        return str;
    }
}
