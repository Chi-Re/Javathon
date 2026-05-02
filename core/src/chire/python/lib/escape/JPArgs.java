package chire.python.lib.escape;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JPArgs {
    private final Map<String, Object> args = new LinkedHashMap<>();

    private final List<String> keys;

    public JPArgs(String... keys) {
        this.keys = List.of(keys);
    }

    public void setArgs(JPArg... args) {
        boolean keyMode = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].key != null) {
                keyMode = true;

                this.args.put(args[i].key, args[i].object);
            } else if (keyMode) {
                throw new RuntimeException("no key");
            } else {
                this.args.put(this.keys.get(i), args[i].object);
            }
        }
    }

    public void setArgs(Object... objects) {
        boolean keyMode = false;

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof JPArg) {
                keyMode = true;

                args.put(((JPArg) objects[i]).key, ((JPArg) objects[i]).object);
            } else if (keyMode){
                throw new RuntimeException("no key");
            } else {
                args.put(this.keys.get(i), objects[i]);
            }
        }
    }

    public Object get(String name) {
        return this.args.get(name);
    }

    public static class JPArg{
        Integer current;
        Object object;
        String key;

        /**注意！Integer是必须的，尤其是在兼容层。*/
        public JPArg(Integer current, Object object) {
            this.current = current;
            this.object = object;
            this.key = null;
        }

        public JPArg(String key, Object object) {
            this.key = key;
            this.object = object;
            this.current = -1;
        }
    }
}
