// 実行: java WaitTasks.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaitTasks {
    // 1つのタスクが「いつからいつまで、どのスレッドで」動いたかの記録
    record Span(String mode, String task, String thread, long startMs, long endMs) {}

    static final long T0 = System.nanoTime();
    static final List<Span> spans = Collections.synchronizedList(new ArrayList<>());

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    // 応答を200ミリ秒待つ処理（ネットワークやデータベースの待ちを、sleep で模擬する）
    static void callRemote(String mode, String task) throws InterruptedException {
        long start = nowMs();
        Thread.sleep(200);
        spans.add(new Span(mode, task, Thread.currentThread().getName(), start, nowMs()));
    }

    public static void main(String[] args) throws Exception {
        // 1. 順番に（同期的に）8回呼ぶ
        long start = nowMs();
        for (int i = 1; i <= 8; i++) {
            callRemote("sequential", "task-" + i);
        }
        System.out.println("順番に実行: " + (nowMs() - start) + " ms");

        // 2. 8本のスレッドで同時に呼ぶ
        start = nowMs();
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String task = "task-" + i;
            Thread t = new Thread(() -> {
                try {
                    callRemote("concurrent", task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "worker-" + i);
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) t.join();  // すべてのスレッドが終わるまで待つ
        System.out.println("同時に実行: " + (nowMs() - start) + " ms");

        System.out.println("mode,task,thread,start_ms,end_ms");
        for (Span s : spans) {
            System.out.printf("%s,%s,%s,%d,%d%n", s.mode(), s.task(), s.thread(), s.startMs(), s.endMs());
        }
    }
}
