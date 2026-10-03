// 実行: java PoolSize.java cpu 4
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PoolSize {
    static final int TASKS = 64;
    static volatile long sink;

    // 待つ処理：応答を50ミリ秒待つ（sleep で模擬する）
    static void waitTask() throws InterruptedException {
        Thread.sleep(50);
    }

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

    // 大きさ poolSize のプールで TASKS 個のタスクを実行し、すべて終わるまでの時間を返す
    static long run(String kind, int poolSize) throws Exception {
        long start = System.nanoTime();
        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < TASKS; i++) {
                futures.add(pool.submit(() -> {
                    if (kind.equals("cpu")) sink = cpuTask();
                    else waitTask();
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();  // すべて終わるのを待つ
        }  // close()：プールを閉じ、スレッドが終わるのを待つ
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        String kind = args.length > 0 ? args[0] : "cpu";
        int poolSize = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        run(kind, poolSize);  // 準備運動（JITコンパイルさせる）
        System.out.printf("%s,%d,%d%n", kind, poolSize, run(kind, poolSize));
    }
}
