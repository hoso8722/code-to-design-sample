// 実行: java TimeoutAndCancel.java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutAndCancel {
    static final long T0 = System.nanoTime();
    static volatile long sink;

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %-18s %s%n", nowMs(), Thread.currentThread().getName(), message);
    }

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            // 1. get(timeout)：100ミリ秒で待つのをやめる。タスクはどうなるか
            Future<String> slow = pool.submit(() -> {
                log("slow: 開始");
                Thread.sleep(300);
                log("slow: 最後まで動いて終わった");
                return "slow";
            });
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log("main: 100ミリ秒で待つのをやめた（TimeoutException）");
            }
            slow.get();  // タスクが終わるまで待って、次の実験へ

            // 2. cancel(true)：割り込みに応じるタスクと、応じないタスク
            long cancelAt = nowMs() + 100;
            Future<?> polite = pool.submit(() -> {
                log("polite: 開始");
                try {
                    Thread.sleep(1000);                    // sleep は割り込まれると InterruptedException を投げる
                    log("polite: 最後まで動いた");
                } catch (InterruptedException e) {
                    log("polite: 割り込まれたので止まった");
                }
            });
            Future<?> stubborn = pool.submit(() -> {
                log("stubborn: 開始");
                long end = System.nanoTime() + 1_000_000_000L;
                while (System.nanoTime() < end) sink++;  // 割り込みを確認しない計算（約1秒）
                log("stubborn: 最後まで動いた（割り込みを無視した）");
            });
            Thread.sleep(Math.max(0, cancelAt - nowMs()));
            log("main: 2つとも cancel(true) する");
            polite.cancel(true);
            stubborn.cancel(true);
            log("main: polite の状態 = " + polite.state() + "、stubborn の状態 = " + stubborn.state());
        }
        log("main: プールを閉じ終わった");
    }
}
