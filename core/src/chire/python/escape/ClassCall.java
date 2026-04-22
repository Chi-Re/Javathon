package chire.python.escape;

public class ClassCall {
    public final Object obj;

    public ClassCall(Object object) {
        this.obj = object;
    }

    public ClassCall call(String name) {
        if (!(obj instanceof Class<?>)) throw new RuntimeException("no key");
        return new ClassCall(JPUtil.callStaticVar((Class<?>) this.obj, name));
    }

    public ClassCall callMethod(String name, Object... args) {
        if (obj instanceof Class<?>) throw new RuntimeException("no key");
        return new ClassCall(JPUtil.callMethod(this.obj, name, args));
    }
}
