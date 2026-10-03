// 実行: java InvokeAny.java
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InvokeAny {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %-18s %s%n", nowMs(), Thread.currentThread().getName(), message);
    }

    // 同じ問い合わせを複数の相手に送り、応答時間が違う、という想定のタスク
    static Callable<String> replica(String name, long ms) {
        return () -> {
            try {
                Thread.sleep(ms);
                log(name + ": 応答した");
                return name;
            } catch (InterruptedException e) {
                log(name + ": 割り込まれて止まった");
                throw e;
            }
        };
    }

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            List<Callable<String>> tasks = List.of(replica("A(300ms)", 300), replica("B(100ms)", 100), replica("C(200ms)", 200));

            String first = pool.invokeAny(tasks);  // 最初に成功した1つの結果
            log("main: invokeAny の結果 = " + first);

            var all = pool.invokeAll(tasks);       // すべて終わるまで待つ
            log("main: invokeAll の結果 = " + all.stream().map(f -> f.resultNow()).toList());
        }
    }
}
