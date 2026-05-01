package chire.python.escape;

import chire.python.lib.PyList;
import chire.python.lib.base.PyFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JPUtil {
    public static final Map<String, PyFunction> funs = new HashMap<>(){{
        put("print", new PyFunction(args -> {
            for (int i = 0; i < args.length; i++) {
                System.out.print(args[i]);
                if (i + 1 < args.length) System.out.print(" ");
            }
            System.out.println();

            return null;
        }, void.class));

        put("int", new PyFunction(args -> {
            return new BaseValue<>((int) args[0], int.class);
        }, int.class));

        put("range", new PyFunction<>(args -> {
            return new PyList(IntStream.iterate((Integer) args[0], n -> n + 1)
                    .limit((Integer) args[1]-1)
                    .boxed()
                    .collect(Collectors.toList()));
        }, PyList.class));
    }};

    static class BaseValue<T> {
        T value;

        Class<T> type;

        public BaseValue(T value, Class<T> type) {
            this.value = value;
            this.type = type;
        }

        public T getValue() {
            return value;
        }

        public Class<T> getType() {
            return type;
        }
    }

    public static PyList asPyList(Object... objects) {
        return new PyList(objects);
    }

    public static PyList iterator(Object key) {
        if (key instanceof PyList) {
            return ((PyList) key);
        } else {
            throw new RuntimeException("no key");
        }
    }

    public static Object operation(Object k, Object p, String f) {
        if (k instanceof Integer && p instanceof Integer) {
            return operationInt((Integer) k, (Integer) p, f);
        }

        return -1;
    }

    public static Integer operationInt(Integer k, Integer p, String f) {
        return switch (f) {
            case "+" -> k + p;
            case "-" -> k - p;
            case "*" -> k * p;
            case "/" -> k / p;
            default -> throw new RuntimeException("no key");
        };
    }

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

    public static void setVar(Object type, String name, Object value) {
        try {
            if (type instanceof Class<?>) {
                ((Class<?>) type).getDeclaredField(name).set(null, value);
            } else {
                type.getClass().getDeclaredField(name).set(type, value);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
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
        List<Object> reArgs = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof BaseValue) {
                classes.add(((BaseValue) arg).getType());
                reArgs.add(((BaseValue) arg).getValue());
                continue;
            }
            classes.add(arg.getClass());
            reArgs.add(arg);
        }

        try {
            if (obj == null) throw new NoSuchMethodException();

            //TODO 可能存在int.class这一类的存在，未修复。
            if (obj instanceof Class<?>) {
                return ((Class<?>) obj).getMethod(name, classes.toArray(new Class[0])).invoke(null, reArgs.toArray(new Object[0]));
            } else {
                return obj.getClass().getMethod(name, classes.toArray(new Class[0])).invoke(obj, reArgs.toArray(new Object[0]));
            }

        } catch (NoSuchMethodException e) {
            return funs.get(name).call(args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
