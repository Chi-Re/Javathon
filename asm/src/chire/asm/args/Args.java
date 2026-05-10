package chire.asm.args;

import chire.asm.util.Format;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Args {
    private final Map<String, String> args;

    public Args() {
        this.args = new LinkedHashMap<>();
    }

    public String[] getArgNames() {
        return args.keySet().toArray(new String[0]);
    }

    public void put(String key, Class<?> value) {
        args.put(key, Format.formatPack(value));
    }

    public void put(String key, String value) {
        args.put(key, value);
    }

    @Override
    public String toString() {
        return Format.formatStrPacks(args.values().toArray(new String[0]));
    }
}
