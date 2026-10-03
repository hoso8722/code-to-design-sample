// 実行: java RunVsStart.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunVsStart {
    static final long T0 = System.nanoTime();
    static final List<String> records = Collections.synchronizedList(new ArrayList<>());

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    // 100ミリ秒待ってから、どのスレッドで動いたかを記録するタスク
    static Runnable task(String name) {
        return () -> {
            long start = nowMs();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            records.add(name + "," + Thread.currentThread().getName() + "," + start + "," + nowMs());
        };
    }

    public static void main(String[] args) throws Exception {
        // 1. run() を直接呼ぶ：ただのメソッド呼び出し
        for (int i = 1; i <= 3; i++) {
            Thread t = new Thread(task("run-" + i), "worker-run-" + i);
            t.run();
        }

        // 2. start() する：新しいスレッドで動く
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Thread t = new Thread(task("start-" + i), "worker-start-" + i);
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) t.join();

        System.out.println("task,thread,start_ms,end_ms");
        records.forEach(System.out::println);
    }
}
