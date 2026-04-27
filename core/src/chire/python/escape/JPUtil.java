package chire.python.escape;

import chire.python.lib.base.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class JPUtil {
    public static final Map<String, PyFunction> funs = new HashMap<>(){{
        put("print", new PyFunction(args -> {
            for (int i = 0; i < args.length; i++) {
                System.out.print(args[i]);
                if (i + 1 < args.length) System.out.print(" ");
            }

            return null;
        }));
    }};

    public static boolean comparison(Object k, Object p, String f) {
        if (k instanceof Integer && p instanceof Integer) {
            return compareInt(((Integer) k), ((Integer) p), f);
        }

        return false;
    }

    private static boolean compareInt(Integer k, Integer p, String f) {
        return switch (f) {
            case ">" -> k > p;
            case "<" -> k < p;
            case "==" -> Objects.equals(k, p);
            case ">=" -> compareInt(k, p, ">") || compareInt(k, p, "==");
            case "<=" -> compareInt(k, p, "<") || compareInt(k, p, "==");
            case "!=" -> !compareInt(k, p, "==");
            default -> false;
        };
    }

    public static Object newInstance(Class<?> type, Object... args) {
        List<Class<?>> classes = new ArrayList<>();

        for (Object arg : args) {
            classes.add(arg.getClass());
        }

        try {
            return type.getConstructor(classes.toArray(new Class[0])).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object callVar(Object type, String name) {
        try {
            if (type instanceof Class<?>) {
                return ((Class<?>) type).getDeclaredField(name).get(null);
            } else {
                return type.getClass().getDeclaredField(name).get(type);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object callMethod(Object obj, String name, Object... args) {
        List<Class<?>> classes = new ArrayList<>();

        for (Object arg : args) {
            classes.add(arg.getClass());
        }

        try {
            if (obj == null) throw new NoSuchMethodException();

            //TODO 可能存在int.class这一类的存在，未修复。
            if (obj instanceof Class<?>) {
                return ((Class<?>) obj).getMethod(name, classes.toArray(new Class[0])).invoke(null, args);
            } else {
                return obj.getClass().getMethod(name, classes.toArray(new Class[0])).invoke(obj, args);
            }

        } catch (NoSuchMethodException e) {
            return funs.get(name).call(args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
