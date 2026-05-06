package chire.python.lib.escape;

import chire.python.lib.PyTuple;

import java.util.*;

public class JPArgs {
    private final Map<String, Object> args = new LinkedHashMap<>();

    private final List<String> keys;

    public JPArgs(String... keys) {
        this.keys = new ArrayList<>();
        Collections.addAll(this.keys, keys);
    }

    public void setArgs(Object... objects) {
        this.args.putAll(to(this.keys.toArray(new String[0]), objects));
    }

    public static Map<String, Object> to(String[] keys, Object[] objects) {
        boolean keyMode = false;
        Map<String, Object> args = new HashMap<>();

        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof JPArg) {
                keyMode = true;
                args.put(((JPArg) objects[i]).key, ((JPArg) objects[i]).object);
                continue;
            } else if (keyMode){
                throw new RuntimeException("no key");
            }

            if (keys[i].startsWith("*")) {
                String argKey = keys[i].replaceFirst("\\*", "");
                List<Object> list = new ArrayList<>();

                for (int j = i; j < objects.length; j++) {
                    list.add(objects[j]);

                    i = j;
                    if (j + 1 < objects.length && objects[j + 1] instanceof JPArg) {
                        break;
                    }
                }

                args.put(argKey, PyTuple.fromArray(list.toArray()));

                continue;
            }

            args.put(keys[i], objects[i]);
        }

        return args;
    }

    public Object get(String name) {
        return this.args.get(name);
    }

    public static class JPArg{
        final Object object;
        final String key;

        public JPArg(String key) {
            this.key = key;
            this.object = null;
        }

        public JPArg(String key, Object object) {
            this.key = key;
            this.object = object;
        }
    }
}
