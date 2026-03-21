package chire.asm.test;

public class Test {
    public static void main(String[] var0) {
        Runnable run = () -> {
            String str = "Hello?";

            System.out.println("Hello World");
            System.out.println(str);
        };

        run.run();
    }
}


