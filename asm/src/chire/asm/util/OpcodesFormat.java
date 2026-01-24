package chire.asm.util;

import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpcodesFormat {
    private static Map<Integer, Integer> opcodes = new HashMap<Integer, Integer>() {{
        put(Opcodes.ACC_PUBLIC, Opcodes.PUTFIELD);
    }};

    public static int to(int opcode) {
        return opcodes.get(opcode);
    }
}
