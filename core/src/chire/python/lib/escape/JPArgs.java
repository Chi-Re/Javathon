package chire.python.lib.escape;

import chire.python.lib.PyList;
import chire.python.lib.PyTuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JPArgs {
    private final Map<String, Object> args = new LinkedHashMap<>();

    private final List<String> keys;

    public JPArgs(String... keys) {
        this.keys = List.of(keys);
    }

//    public void setArgs(JPArg... args) {
//        boolean keyMode = false;
//
//        for (int i = 0; i < args.length; i++) {
//            if (args[i].key != null) {
//                keyMode = true;
//
//                this.args.put(args[i].key, args[i].object);
//            } else if (keyMode) {
//                throw new RuntimeException("no key");
//            } else {
//                this.args.put(this.keys.get(i), args[i].object);
//            }
//        }
//    }

    public void setArgs(Object... objects) {
//        boolean keyMode = false;
//
//        for (int i = 0; i < objects.length; i++) {
//            if (objects[i] instanceof JPArg) {
//                keyMode = true;
//
//                args.put(((JPArg) objects[i]).key, ((JPArg) objects[i]).object);
//            } else if (keyMode){
//                throw new RuntimeException("no key");
//            } else {
//                args.put(this.keys.get(i), objects[i]);
//            }
//        }
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

                    if (j + 1 < objects.length && objects[j + 1] instanceof JPArg) {
                        i = j;
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
