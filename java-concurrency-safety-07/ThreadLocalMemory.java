// 実行: java ThreadLocalMemory.java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadLocalMemory {
    static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<>();
    static final int THREADS = 20_000;
    static final int SIZE = 10 * 1024; // スレッドごとに持たせるバッファ(10キロバイト)

    static long usedMb() {
        System.gc();
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) throws Exception {
        long base = usedMb();
        System.out.println("  開始時の使用ヒープ: " + base + " MB");

        // 仮想スレッド2万本が、それぞれ ThreadLocal にバッファを持ったまま、待っている状態
        CountDownLatch ready = new CountDownLatch(THREADS), release = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) executor.submit(() -> {
                BUFFER.set(new byte[SIZE]);
                ready.countDown();
                try { release.await(); } catch (InterruptedException ignored) { }
                BUFFER.remove();
            });
            ready.await();
            System.out.println("  仮想スレッド " + THREADS + " 本が、ThreadLocal のバッファ(" + SIZE / 1024 + " KB)を持って待っている間の使用ヒープ: " + usedMb() + " MB(増加 " + (usedMb() - base) + " MB)");
            release.countDown();
        }

        // 同じ2万件の処理を、16本のスレッドプールで行い、1件ごとに ThreadLocal を片付ける
        CountDownLatch gate = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(16)) {
            for (int i = 0; i < THREADS; i++) pool.submit(() -> {
                BUFFER.set(new byte[SIZE]);
                try { gate.await(); } catch (InterruptedException ignored) { }
                BUFFER.remove();
            });
            Thread.sleep(500); // 16本が待ち状態になるのを待つ
            System.out.println("  16本のスレッドプールで同じ処理をして待っている間の使用ヒープ: " + usedMb() + " MB(増加 " + (usedMb() - base) + " MB)");
            gate.countDown();
        }
    }
}
