package chire.python.lib.builtins;

import chire.python.lib.PyTypes;

import java.util.HashMap;
import java.util.Map;

public class PyObject {
    /**关于私有变量，一般是存储在此*/
    public static PyDict __dict__;
    /**存储所有的数据，包括__dict__*/
    public final Map<String, PyObject> attrs = new HashMap<>();

    static {
        PyTypes.initType();
    }

    public PyObject(){
        __init__(this);
    }

    /**
     * @return null
     */
    public static PyObject __init__(PyObject self) {
        //TODO 关于py与java对应关系这方面我并没有寻思好该如何
//        self.attrs.update("__name__", "object");

        return null;
    }
}