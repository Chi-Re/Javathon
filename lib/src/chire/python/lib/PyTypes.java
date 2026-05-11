package chire.python.lib;

import chire.python.lib.builtins.PyDict;
import chire.python.lib.builtins.PyList;
import chire.python.lib.builtins.PyObject;
import chire.python.lib.builtins.PyTuple;
import chire.python.lib.escape.JPFunction;
import chire.python.lib.escape.JPUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PyTypes {
    public static final Map<String, JPFunction> funs = new HashMap<String, JPFunction>(){{
        put("print", new JPFunction(
                new String[]{"*args", "end"},
                args -> {
                    PyTuple iargs = ((PyTuple) args.getOrDefault("args", PyTuple.empty()));
                    String sep = args.getOrDefault("sep", " ").toString();
                    String end = args.getOrDefault("end", "\n").toString();

                    for (int i = 0; i < iargs.size(); i++) {
                        System.out.print(iargs.get(i));

                        if (i + 1 < iargs.size()) System.out.print(sep);
                    }

                    System.out.print(end);

                    return null;
                },
                void.class
        ));

        put("int", new JPFunction<>(new String[]{"obj"}, args -> {
            return new JPUtil.BaseValue<>((int) args.get("obj"), int.class);
        }, int.class));

        put("range", new JPFunction<>(new String[]{"in", "to"}, args -> {
            if (args.size() == 2) {
                PyList list = new PyList();

                for (int i = (int) args.get("in"); i < (int) args.get("to"); i++) list.append(i);

                return list;
            } else if (args.size() == 1) {
                PyList list = new PyList();

                for (int i = 0; i < (int) args.get("in"); i++) list.append(i);

                return list;
            } else {
                throw new RuntimeException("no key");
            }
        }, PyList.class));

        put("len", new JPFunction<>(new String[]{"item"}, args -> {
            Object item = args.get("item");

            if (item instanceof PyList) {
                return ((PyList) item).__len__();
            }
            if (item instanceof PyDict) {
                return ((PyDict) item).__len__();
            }
            if (item instanceof PyTuple) {
                return ((PyTuple) item).size();
            }

            if (item instanceof Map<?,?>) {
                return ((Map<?, ?>) item).size();
            }
            if (item instanceof Collection<?>) {
                return ((Collection<?>) item).size();
            }

            if (item instanceof String) {
                return ((String) item).length();
            }
            throw new RuntimeException("no key");
        }, Integer.class));
    }};

    public static Map<String, Class<?>> types = new HashMap<String, Class<?>>(){{
        put("dict", PyDict.class);
        put("tuple", PyTuple.class);
        put("list", PyList.class);
        put("object", PyObject.class);
    }};

    public static void initType(){
        for (String ke : types.keySet()) {
            initClass(types.get(ke));
        }
    }

    public static void initClass(Class<?> clazz) {
        try {
            clazz.getDeclaredField("__dict__").set(null, getClassDict(clazz));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static PyDict getClassDict(Class<?> clazz) {
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
