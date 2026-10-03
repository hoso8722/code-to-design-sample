// 実行: java ManyWaits.java virtual wait
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ManyWaits {
    static volatile long sink;

    // CPUを使う処理：2 から 400,000 までの素数を数える（この環境で1回およそ50ミリ秒）
    static long cpuTask() {
        long count = 0;
        for (int n = 2; n < 400_000; n++) {
            boolean prime = true;
            for (int d = 2; (long) d * d <= n; d++) {
                if (n % d == 0) {
                    prime = false;
                    break;
                }
            }
            if (prime) count++;
        }
        return count;
    }

    static ExecutorService executor(String kind) {
        return switch (kind) {
            case "fixed100" -> Executors.newFixedThreadPool(100);
            case "fixed1000" -> Executors.newFixedThreadPool(1000);
            case "fixed16" -> Executors.newFixedThreadPool(16);
            default -> Executors.newVirtualThreadPerTaskExecutor();  // タスクごとに仮想スレッドを1本作る
        };
    }

    static long run(String kind, String work, int tasks) throws Exception {
        long start = System.nanoTime();
        try (ExecutorService executor = executor(kind)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                futures.add(executor.submit(() -> {
                    if (work.equals("wait")) Thread.sleep(100);  // 100ミリ秒の待ち（sleep で模擬する）
                    else sink = cpuTask();
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        String kind = args.length > 0 ? args[0] : "virtual";  // fixed16 / fixed100 / fixed1000 / virtual
        String work = args.length > 1 ? args[1] : "wait";     // wait / cpu
        int tasks = work.equals("wait") ? 10_000 : 64;
        run(kind, work, tasks);  // 準備運動
        System.out.printf("%s,%s,%d,%d%n", kind, work, tasks, run(kind, work, tasks));
    }
}
