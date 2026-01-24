package chire.asm.util;

import chire.asm.args.Args;
import org.objectweb.asm.Opcodes;

public class Format {
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
        return formatPacks(parameterTypes)+(returnType==null?"V":(Format.formatPack(returnType)+";"));
    }
}
