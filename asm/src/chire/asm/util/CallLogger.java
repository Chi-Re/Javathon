package chire.asm.util;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

// 拦截器
public class CallLogger {
    private final List<String> callLog = new ArrayList<>();

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args, @SuperCall Callable<?> zuper) throws Exception {
        String logEntry = method.getName() + "(" + java.util.Arrays.toString(args) + ")";
        callLog.add(logEntry);
        System.out.println("[CALL] " + logEntry);
        return zuper.call(); // 调用原始方法
    }

    public void printCallSequence() {
        System.out.println("\n===== Method Call Sequence =====");
        for (int i = 0; i < callLog.size(); i++) {
            System.out.println((i+1) + ". " + callLog.get(i));
        }
    }
}
