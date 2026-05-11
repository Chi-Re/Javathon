package chire.python.lib.builtins;

import chire.python.lib.PyTypes;
import chire.python.lib.escape.JPFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class PyObject {
    /**关于私有变量，一般是存储在此*/
    public static PyDict __dict__;
    /**存储所有的数据，包括__dict__*/
    protected final Map<String, PyObject> attrs = new HashMap<>();

    static {
        PyTypes.initType();
    }

    public PyObject(){
        __init__(this);
    }

    public static void __init__(PyObject self) {
        //TODO 关于py与java对应关系这方面我并没有寻思好该如何
//        self.globals.update("__name__", "object");
    }
}