// 実行: java SyncTools.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncTools {
    // ===== 1. 4つの道具 =====
    static void tools() throws Exception {
        // CountDownLatch: 「N個の作業が終わるまで待つ」。1回きり
        CountDownLatch done = new CountDownLatch(3);
        AtomicInteger finished = new AtomicInteger();
        for (int i = 0; i < 3; i++) new Thread(() -> { sleep(50); finished.incrementAndGet(); done.countDown(); }).start();
        done.await();
        System.out.println("  CountDownLatch: await が戻った時点で完了した作業 = " + finished.get() + "(3個)");

        // CyclicBarrier: 「全員がそろうまで待つ」。繰り返し使える
        AtomicInteger trips = new AtomicInteger();
        CyclicBarrier barrier = new CyclicBarrier(3, trips::incrementAndGet);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread t = new Thread(() -> {
                try { for (int round = 0; round < 4; round++) { sleep(10); barrier.await(); } } catch (Exception e) { throw new RuntimeException(e); }
            });
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) t.join();
        System.out.println("  CyclicBarrier: 3人がそろった回数 = " + trips.get() + "(4ラウンド)");

        // Semaphore: 「同時に入れる数を制限する」
        Semaphore permits = new Semaphore(3);
        AtomicInteger inside = new AtomicInteger(), maxInside = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(12);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 24; i++) futures.add(pool.submit(() -> {
            try {
                permits.acquire();
                try {
                    int now = inside.incrementAndGet();
                    maxInside.accumulateAndGet(now, Math::max);
                    sleep(20);
                } finally {
                    inside.decrementAndGet();
                    permits.release();
                }
            } catch (InterruptedException ignored) { }
        }));
        for (var f : futures) f.get();
        pool.shutdown();
        System.out.println("  Semaphore(3): 12本のスレッドのうち、同時に中にいた最大数 = " + maxInside.get());

        // Phaser: 参加者の数が途中で変わる、段階的な同期
        Phaser phaser = new Phaser(1); // 自分(main)が参加者
        AtomicInteger phase0 = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            phaser.register();
            new Thread(() -> { phase0.incrementAndGet(); phaser.arriveAndAwaitAdvance(); phaser.arriveAndDeregister(); }).start();
        }
        phaser.arriveAndAwaitAdvance(); // 第0フェーズ: 3人の作業が終わるのを待つ
        System.out.println("  Phaser: 第0フェーズが終わった時点の完了数 = " + phase0.get() + "、フェーズ番号 = " + phaser.getPhase());
        phaser.arriveAndDeregister();
    }

    static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ===== 2. 仮想スレッドを大量に動かすとき、同時実行数を制限する =====
    static void limitVirtualThreads() throws Exception {
        int tasks = 2000;
        for (int limit : new int[] {0, 10}) { // 0 は制限なし
            Semaphore semaphore = limit > 0 ? new Semaphore(limit) : null;
            AtomicInteger inside = new AtomicInteger(), maxInside = new AtomicInteger();
            long t0 = System.nanoTime();
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < tasks; i++) executor.submit(() -> {
                    try {
                        if (semaphore != null) semaphore.acquire();
                        try {
                            int now = inside.incrementAndGet();
                            maxInside.accumulateAndGet(now, Math::max);
                            sleep(20); // 外部サービスの呼び出しを模す
                        } finally {
                            inside.decrementAndGet();
                            if (semaphore != null) semaphore.release();
                        }
                    } catch (InterruptedException ignored) { }
                });
            }
            System.out.printf("  %-12s %d 件のタスク(各20ミリ秒): 同時に動いた最大数 %d、全体で %d ミリ秒%n",
                    limit > 0 ? "Semaphore(" + limit + ")" : "制限なし", tasks, maxInside.get(), (System.nanoTime() - t0) / 1_000_000);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 4つの道具");
        tools();
        System.out.println("## 2. 仮想スレッド2000本と Semaphore");
        limitVirtualThreads();
    }
}
