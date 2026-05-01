package chire.python.lib.escape;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JPArgs {
    private final Map<String, Object> args = new LinkedHashMap<>();

    private final String[] keys;

    public JPArgs(String... keys) {
        this.keys = keys;
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
                this.args.put(this.keys[i], args[i].object);
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
