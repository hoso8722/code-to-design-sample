// 実行: java WhichThread.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class WhichThread {
    // どのスレッドで動いたかを出力して、値をそのまま返す
    static <T> T log(String step, T value) {
        System.out.printf("  %-38s %s%n", Thread.currentThread().getName(), step);
        return value;
    }

    static String fetch() {
        try {
            Thread.sleep(100);  // 100ミリ秒の応答待ち（sleep で模擬する）
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return log("1. fetch", "data");
    }

    public static void main(String[] args) {
        System.out.println("commonPool の並列度: " + ForkJoinPool.getCommonPoolParallelism());

        try (ExecutorService io = Executors.newFixedThreadPool(2, Thread.ofPlatform().name("io-", 1).factory())) {
            System.out.println("== A. Executor を渡さない");
            CompletableFuture.supplyAsync(WhichThread::fetch)
                    .thenApply(s -> log("2. thenApply", s.length()))
                    .join();

            System.out.println("== B. 1段目を io プールで動かし、thenApply と thenApplyAsync をつなぐ");
            CompletableFuture.supplyAsync(WhichThread::fetch, io)
                    .thenApply(s -> log("2. thenApply", s.length()))
                    .thenApplyAsync(n -> log("3. thenApplyAsync", n))
                    .thenApplyAsync(n -> log("4. thenApplyAsync（io を渡す）", n), io)
                    .join();

            System.out.println("== C. すでに終わっている CompletableFuture に thenApply をつなぐ");
            CompletableFuture.completedFuture("data")
                    .thenApply(s -> log("2. thenApply", s.length()))
                    .join();
        }
    }
}
