package chire.python.escape;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class JPUtil {
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

    public static Object callStaticVar(Class<?> type, String name) {
        try {
            return type.getDeclaredField(name).get(null);
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
            if (obj instanceof Class<?>) {
                return ((Class<?>) obj).getMethod(name, classes.toArray(new Class[0])).invoke(null, args);
            } else {
                return obj.getClass().getMethod(name, classes.toArray(new Class[0])).invoke(obj, args);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
