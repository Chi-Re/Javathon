package chire.python.lib.types;

import chire.python.lib.builtins.PyDict;
import chire.python.lib.builtins.PyObject;

public class PyClass extends PyObject {
    public static PyDict __dict__ = new PyDict(){{
        //TODO 未完成部分获取，可能需要新的设计代码
    }};

    public PyClass(Class<?> clazz) {

    }

    public static PyObject __init__(PyObject self) {
        PyObject.__init__(self);

        self.attrs.put("__dict__", new PyDict());

        return null;
    }
}
