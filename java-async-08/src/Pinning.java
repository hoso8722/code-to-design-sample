// 実行: java Pinning.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Pinning {
    static final long T0 = System.nanoTime();
    static final List<String> records = Collections.synchronizedList(new ArrayList<>());

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    // 仮想スレッドの toString() は「VirtualThread[#番号]/runnable@キャリアスレッドの名前」の形
    static String carrier() {
        String s = Thread.currentThread().toString();
        return s.substring(s.indexOf('@') + 1);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("java.version") + "、CPU " + Runtime.getRuntime().availableProcessors());
        long start = nowMs();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                String task = "task-" + i;
                Object lock = new Object();  // タスクごとに別のロック（ロックの取り合いはない）
                futures.add(executor.submit(() -> {
                    synchronized (lock) {
                        long begin = nowMs();
                        String before = carrier();
                        Thread.sleep(100);  // synchronized の中で100ミリ秒待つ（sleep で模擬する）
                        records.add(task + "," + before + "," + begin + "," + nowMs());
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) f.get();
        }
        System.out.println("64個のタスクが終わるまで: " + (nowMs() - start) + " ms");
        System.out.println("task,carrier,start_ms,end_ms");
        records.forEach(System.out::println);
    }
}
