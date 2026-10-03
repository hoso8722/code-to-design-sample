// 実行: java TryLock.java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TryLock {
    static final ReentrantLock accountA = new ReentrantLock();
    static final ReentrantLock accountB = new ReentrantLock();

    // 2つのロックを tryLock で取る。取れなければ、持っているロックを手放してやり直す
    static void transfer(String name, ReentrantLock first, ReentrantLock second) throws InterruptedException {
        int attempts = 0;
        while (true) {
            attempts++;
            if (first.tryLock(50, TimeUnit.MILLISECONDS)) {
                try {
                    Thread.sleep(100);  // 相手が1つ目のロックを取るのを待つ（タイミングを合わせるため）
                    if (second.tryLock(50, TimeUnit.MILLISECONDS)) {
                        try {
                            System.out.println(name + ": 2つのロックを取れた（" + attempts + " 回目）");
                            return;
                        } finally {
                            second.unlock();
                        }
                    }
                } finally {
                    first.unlock();
                }
            }
            System.out.println(name + ": 取れなかったので、手放してやり直す");
            Thread.sleep((long) (Math.random() * 100));  // 少し待ってから（待つ時間をずらす）
        }
    }

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        Thread t1 = new Thread(() -> run("transfer-1", accountA, accountB), "transfer-1");
        Thread t2 = new Thread(() -> run("transfer-2", accountB, accountA), "transfer-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("main: 2つとも終わった（" + (System.nanoTime() - start) / 1_000_000 + " ms）");
    }

    static void run(String name, ReentrantLock first, ReentrantLock second) {
        try {
            transfer(name, first, second);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
