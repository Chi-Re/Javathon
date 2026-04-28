package chire.python.lib.base;

public class PyFunction<T> {
    Parameter content;
    Class<T> returnType;

    public PyFunction(Parameter content, Class<T> returnType) {
        this.content = content;
        this.returnType = returnType;
    }

    public T call(Object... args) {
        //TODO 类型可能存在强制转换错误
        return (T) this.content.invoke(args);
    }

    public interface Parameter{
        Object invoke(Object... args);
    }
}
