package chire.python.lib.base;

public class PyFunction {
    Parameter content;

    public PyFunction(Parameter content) {
        this.content = content;
    }

    public Object call(Object... args) {
        return this.content.invoke(args);
    }

    public interface Parameter{
        Object invoke(Object... args);
    }
}
