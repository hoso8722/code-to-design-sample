// 実行: java Timeouts.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class Timeouts {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %-36s %s%n", nowMs(), Thread.currentThread().getName(), message);
    }

    // 300ミリ秒かかる問い合わせ（待ちは sleep で模擬する）
    static String slowQuery(String name) {
        log(name + ": 開始");
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            log(name + ": 割り込まれた");
            throw new CompletionException(e);
        }
        log(name + ": 最後まで動いて終わった");
        return name + " の結果";
    }

    public static void main(String[] args) throws Exception {
        // 1. orTimeout：100ミリ秒で TimeoutException にする
        CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> slowQuery("A"))
                .orTimeout(100, TimeUnit.MILLISECONDS);
        try {
            a.join();
        } catch (CompletionException e) {
            log("main: A は " + e.getCause().getClass().getSimpleName() + " で終わった");
        }
        Thread.sleep(300);  // 元の処理がどうなるかを見る

        // 2. completeOnTimeout：100ミリ秒で代わりの値にする
        String b = CompletableFuture.supplyAsync(() -> slowQuery("B"))
                .completeOnTimeout("B の代わりの値", 100, TimeUnit.MILLISECONDS)
                .join();
        log("main: B の結果 = " + b);
        Thread.sleep(300);
    }
}
