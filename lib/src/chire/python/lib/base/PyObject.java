package chire.python.lib.base;

import chire.python.lib.PyDict;
import chire.python.lib.escape.JPFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PyObject {
    /**关于私有变量，一般是存储在此*/
    public static PyDict __dict__;

    protected PyDict globals = new PyDict();

    static {
        init(PyObject.class);
    }

//    public static void __init__(PyObject self) {
//        try {
//            self.getClass().getDeclaredField("__dict__").set(self, null);
//        } catch (IllegalAccessException | NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static void init(Class<?> clazz) {
        try {
            clazz.getDeclaredField("__dict__").set(null, init__dict__(clazz));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static void __dict__(Class<?> type, String name, Object value) {
        __dict__(type).update(name, value);
    }

    public static PyDict __dict__(Class<?> type){
        try {
            return (PyDict) type.getDeclaredField("__dict__").get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static PyDict init__dict__(Class<?> clazz) {
        return new PyDict(){{
            for (Method method : clazz.getMethods()) {
                update(method, new JPFunction<>(new String[]{"self", "*args"}, con -> {
                    try {
                        return method.invoke(con.get("self"), con.get("args"));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }, Object.class));
            }
        }};
    }
}