// 実行: java CpuTimeline.java
import java.util.ArrayList;
import java.util.List;

// CPUを使うタスクを4本のスレッドで同時に動かし、それぞれの開始・終了時刻を記録する
public class CpuTimeline {
    static final long T0 = System.nanoTime();
    static volatile long sink;

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

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

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 5; i++) sink = cpuTask();  // 準備運動（JITコンパイルさせる）
        System.out.println("使えるCPUの数: " + Runtime.getRuntime().availableProcessors());

        long base = nowMs();
        List<Thread> threads = new ArrayList<>();
        List<String> lines = java.util.Collections.synchronizedList(new ArrayList<>());
        for (int i = 1; i <= 4; i++) {
            Thread t = new Thread(() -> {
                long start = nowMs() - base;
                sink = cpuTask();
                lines.add(Thread.currentThread().getName() + "," + start + "," + (nowMs() - base));
            }, "worker-" + i);
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) t.join();
        System.out.println("thread,start_ms,end_ms");
        lines.stream().sorted().forEach(System.out::println);
    }
}
