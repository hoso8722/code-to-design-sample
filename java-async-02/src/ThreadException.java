// 実行: java ThreadException.java
import java.util.ArrayList;
import java.util.List;

public class ThreadException {
    public static void main(String[] args) throws Exception {
        // 1. try-catch で囲んでも、別のスレッドの例外は捕まえられない
        try {
            Thread t = new Thread(() -> {
                throw new IllegalStateException("worker で失敗");
            }, "worker-1");
            t.start();
            t.join();
            System.out.println("main: worker-1 は終わった（例外は届かない）");
        } catch (Exception e) {
            System.out.println("main: 捕まえた " + e);  // ここには来ない
        }

        // 2. UncaughtExceptionHandler で受け取る
        List<Throwable> errors = new ArrayList<>();
        Thread t2 = new Thread(() -> {
            throw new IllegalStateException("worker-2 で失敗");
        }, "worker-2");
        t2.setUncaughtExceptionHandler((thread, e) -> {
            synchronized (errors) {
                errors.add(e);
            }
        });
        t2.start();
        t2.join();
        System.out.println("main: ハンドラが受け取った例外 = " + errors);
    }
}
