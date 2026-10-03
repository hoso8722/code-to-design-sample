// 実行: java ScaleTasks.java cpu 4
import java.util.ArrayList;
import java.util.List;

public class ScaleTasks {
    static final int TASKS = 32;

    // 待つ処理：応答を100ミリ秒待つ（sleep で模擬する）
    static void waitTask() throws InterruptedException {
        Thread.sleep(100);
    }

    // CPUを使う処理：2 から 400,000 までの素数を数える（1回およそ数十ミリ秒）
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

    static volatile long sink;  // 計算結果を使って、JITに計算を消されないようにする

    // TASKS 個のタスクを、threadCount 本のスレッドに順番に割り振って実行し、かかった時間を返す
    static long run(String kind, int threadCount) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < threadCount; t++) {
            int first = t;
            Thread thread = new Thread(() -> {
                for (int task = first; task < TASKS; task += threadCount) {  // t, t+N, t+2N, ... 番目のタスク
                    try {
                        if (kind.equals("cpu")) sink = cpuTask();
                        else waitTask();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) thread.join();
        return (System.nanoTime() - start) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        String kind = args.length > 0 ? args[0] : "cpu";
        int threadCount = args.length > 1 ? Integer.parseInt(args[1]) : 4;
        run(kind, threadCount);  // 準備運動（JITコンパイルさせる）
        System.out.printf("%s,%d,%d%n", kind, threadCount, run(kind, threadCount));
    }
}
