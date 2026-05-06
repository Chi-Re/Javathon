package chire.python.lib.base;

import chire.python.lib.escape.JPArgs;

import java.util.Map;

public class PyFunction<T> {
    String[] argKeys;
    Parameter content;
    Class<T> returnType;

    public PyFunction(String[] argKeys, Parameter content, Class<T> returnType) {
        this.content = content;
        this.returnType = returnType;
        this.argKeys = argKeys;
    }

    public T call(Object... args) {
        //TODO 类型可能存在强制转换错误
        return (T) this.content.invoke(JPArgs.to(argKeys, args));
    }

    public interface Parameter{
        Object invoke(Map<String, Object> args);
    }
}
