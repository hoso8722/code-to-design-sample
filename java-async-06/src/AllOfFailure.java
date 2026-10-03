// 実行: java AllOfFailure.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AllOfFailure {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %-36s %s%n", nowMs(), Thread.currentThread().getName(), message);
    }

    static CompletableFuture<String> task(String name, long ms, boolean fail) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(ms);  // 待ちは sleep で模擬する
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
            if (fail) {
                log(name + ": 失敗した");
                throw new IllegalStateException(name + " で失敗");
            }
            log(name + ": 成功した");
            return name;
        });
    }

    public static void main(String[] args) {
        CompletableFuture<String> a = task("A(100ms, 失敗)", 100, true);
        CompletableFuture<String> b = task("B(300ms)", 300, false);
        CompletableFuture<String> c = task("C(500ms)", 500, false);
        try {
            CompletableFuture.allOf(a, b, c).join();
        } catch (CompletionException e) {
            log("main: allOf が例外で終わった: " + e.getCause().getMessage());
        }
    }
}
