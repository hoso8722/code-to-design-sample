// 実行: java QueueTimeline.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueueTimeline {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        List<Future<String>> futures = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {  // スレッド4本のプール
            for (int i = 1; i <= 16; i++) {
                String task = "task-" + i;
                long submitted = nowMs();  // submit した時刻
                futures.add(pool.submit(() -> {
                    long start = nowMs();  // 実際に動き始めた時刻
                    Thread.sleep(100);     // 100ミリ秒の待ち（sleep で模擬する）
                    return task + "," + Thread.currentThread().getName() + "," + submitted + "," + start + "," + nowMs();
                }));
            }
            System.out.println("task,thread,submitted_ms,start_ms,end_ms");
            for (Future<String> f : futures) System.out.println(f.get());
        }
    }
}
