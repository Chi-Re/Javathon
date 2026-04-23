package chire.python.escape;

public class ClassCall {
    public final Object obj;

    public ClassCall(){
        obj = null;
    }

    public ClassCall(Object object) {
        this.obj = object;
    }

    public ClassCall call(String name) {
        if (this.obj == null) throw new RuntimeException("no key");
        return new ClassCall(JPUtil.callVar(this.obj, name));
    }

    public ClassCall callMethod(String name, Object... args) {
        return new ClassCall(JPUtil.callMethod(this.obj, name, args));
    }
}
