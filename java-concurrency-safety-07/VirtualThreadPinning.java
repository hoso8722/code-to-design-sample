// 実行: java -Djdk.virtualThreadScheduler.parallelism=1 -Djdk.virtualThreadScheduler.maxPoolSize=1 VirtualThreadPinning.java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualThreadPinning {
    static final Object MONITOR = new Object();
    static final ReentrantLock LOCK = new ReentrantLock();

    // 仮想スレッドを動かす土台のスレッド(キャリア)を1本に絞って、
    // 1つ目の仮想スレッドがロックの中で1秒ブロックしている間に、2つ目の仮想スレッドが動けるかを調べる
    static long delayOfSecond(String kind) throws Exception {
        CountDownLatch firstInside = new CountDownLatch(1);
        long[] secondStarted = new long[1];
        long t0 = System.nanoTime();
        Thread first = Thread.ofVirtual().start(() -> {
            if (kind.equals("計算を続ける")) { // 対照: ブロックせずに1秒間、CPUを使い続ける
                firstInside.countDown();
                long end = System.nanoTime() + 1_000_000_000L;
                while (System.nanoTime() < end) { }
            } else if (kind.equals("synchronized")) {
                synchronized (MONITOR) {
                    firstInside.countDown();
                    sleep(1000);
                }
            } else {
                LOCK.lock();
                try {
                    firstInside.countDown();
                    sleep(1000);
                } finally {
                    LOCK.unlock();
                }
            }
        });
        firstInside.await();
        Thread second = Thread.ofVirtual().start(() -> secondStarted[0] = System.nanoTime());
        second.join();
        first.join();
        return (secondStarted[0] - t0) / 1_000_000;
    }

    static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("  JDK: " + System.getProperty("java.version") + "、キャリアの数: parallelism = "
                + System.getProperty("jdk.virtualThreadScheduler.parallelism") + "、maxPoolSize = " + System.getProperty("jdk.virtualThreadScheduler.maxPoolSize"));
        delayOfSecond("synchronized"); // ウォームアップ
        System.out.println("  synchronized の中で1秒ブロック中に、2つ目の仮想スレッドが動き出すまで: " + delayOfSecond("synchronized") + " ミリ秒");
        System.out.println("  ReentrantLock の中で1秒ブロック中に、2つ目の仮想スレッドが動き出すまで: " + delayOfSecond("ReentrantLock") + " ミリ秒");
        System.out.println("  (対照)ブロックせずに1秒計算し続ける間に、2つ目の仮想スレッドが動き出すまで: " + delayOfSecond("計算を続ける") + " ミリ秒");
    }
}
