// 実行: java LockDesign.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class LockDesign {
    static boolean quiet; // ウォームアップ(JITコンパイル)のために、表示せずに1回実行する

    static void print(String format, Object... args) {
        if (!quiet) System.out.printf(format, args);
    }

    static long runThreads(int threads, java.util.function.IntConsumer body) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int id = i;
            Thread t = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                body.accept(id);
            });
            t.start();
            list.add(t);
        }
        long begin = System.nanoTime();
        start.countDown();
        for (Thread t : list) t.join();
        return System.nanoTime() - begin;
    }

    // ===== 1. ロックは「操作」ではなく「データ(不変条件)」に対応させる =====
    // 不変条件: items.size() == count
    static class BadInventory { // items と count を、別々のロックで守っている
        private final List<Integer> items = new ArrayList<>();
        private int count;
        private final Object itemsLock = new Object(), countLock = new Object();
        void add(int x) {
            synchronized (itemsLock) { items.add(x); }
            synchronized (countLock) { count++; }
        }
        int[] snapshot() {
            int size, c;
            synchronized (itemsLock) { size = items.size(); }
            synchronized (countLock) { c = count; }
            return new int[] {size, c};
        }
    }

    static class GoodInventory { // 不変条件に関わるすべてのデータを、同じロックで守る
        private final List<Integer> items = new ArrayList<>();
        private int count;
        synchronized void add(int x) { items.add(x); count++; }
        synchronized int[] snapshot() { return new int[] {items.size(), count}; }
    }

    static void invariantLock() throws Exception {
        for (String kind : new String[] {"別々のロック", "1つのロック"}) {
            BadInventory bad = new BadInventory();
            GoodInventory good = new GoodInventory();
            AtomicInteger observations = new AtomicInteger(), broken = new AtomicInteger();
            AtomicBoolean finished = new AtomicBoolean();
            Thread writer = new Thread(() -> {
                for (int i = 0; i < 300_000; i++) { if (kind.startsWith("別々")) bad.add(i); else good.add(i); }
                finished.set(true);
            });
            Thread observer = new Thread(() -> {
                while (!finished.get()) {
                    int[] s = kind.startsWith("別々") ? bad.snapshot() : good.snapshot();
                    observations.incrementAndGet();
                    if (s[0] != s[1]) broken.incrementAndGet();
                }
            });
            observer.start(); writer.start(); writer.join(); observer.join();
            System.out.printf("  %-12s 観測 %d 回のうち、size と count が一致しなかった回数 %d%n", kind, observations.get(), broken.get());
        }
    }

    // ===== 2. ロックの粒度: 1つの大きなロック / 分割したロック / ConcurrentHashMap =====
    static final int KEYS = 1024;
    static final int OPS_PER_THREAD = 2_000_000;

    static void granularity() throws Exception {
        print("  %-8s %-14s %-18s %-14s%n", "スレッド", "1つのロック", "16分割したロック", "AtomicLongArray");
        for (int threads : new int[] {1, 2, 4, 8, 16}) {
            long[] single = new long[KEYS];
            Object oneLock = new Object();
            long t1 = runThreads(threads, id -> {
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    int k = r.nextInt(KEYS);
                    synchronized (oneLock) { single[k]++; }
                }
            });
            long[] striped = new long[KEYS];
            Object[] locks = new Object[16];
            for (int i = 0; i < locks.length; i++) locks[i] = new Object();
            long t2 = runThreads(threads, id -> {
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    int k = r.nextInt(KEYS);
                    synchronized (locks[k & 15]) { striped[k]++; }
                }
            });
            java.util.concurrent.atomic.AtomicLongArray atomic = new java.util.concurrent.atomic.AtomicLongArray(KEYS);
            long t3 = runThreads(threads, id -> {
                ThreadLocalRandom r = ThreadLocalRandom.current();
                for (int i = 0; i < OPS_PER_THREAD; i++) atomic.incrementAndGet(r.nextInt(KEYS));
            });
            long total = (long) threads * OPS_PER_THREAD;
            long sum1 = 0, sum2 = 0, sum3 = 0;
            for (int i = 0; i < KEYS; i++) { sum1 += single[i]; sum2 += striped[i]; sum3 += atomic.get(i); }
            if (sum1 != total || sum2 != total || sum3 != total) throw new IllegalStateException("合計が合わない");
            print("  %-8d %-14s %-18s %-14s%n", threads, mops(total, t1), mops(total, t2), mops(total, t3));
        }
    }

    static String mops(long ops, long nanos) {
        return String.format("%.0f 万回/秒", ops / (nanos / 1e9) / 1e4);
    }

    // ===== 3. 読み取りが多い場合と書き込みが多い場合 =====
    static class SyncBox {
        private long a, b;
        synchronized void write(long v) { a = v; b = v; }
        synchronized long read() { return a + b; }
    }

    static class RwBox {
        private long a, b;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        void write(long v) { lock.writeLock().lock(); try { a = v; b = v; } finally { lock.writeLock().unlock(); } }
        long read() { lock.readLock().lock(); try { return a + b; } finally { lock.readLock().unlock(); } }
    }

    static class StampedBox {
        private long a, b;
        private final StampedLock lock = new StampedLock();
        void write(long v) { long s = lock.writeLock(); try { a = v; b = v; } finally { lock.unlockWrite(s); } }
        long read() {
            long s = lock.tryOptimisticRead();
            long x = a, y = b;
            if (!lock.validate(s)) { // 読んでいる間に書き込まれた場合は、読み取りロックでやり直す
                s = lock.readLock();
                try { x = a; y = b; } finally { lock.unlockRead(s); }
            }
            return x + y;
        }
    }

    static void readWrite() throws Exception {
        int threads = 8;
        int ops = 2_000_000;
        print("  %-12s %-16s %-24s %-14s%n", "読み取りの割合", "synchronized", "ReentrantReadWriteLock", "StampedLock");
        for (int readPercent : new int[] {99, 90, 50, 1}) {
            String[] cells = new String[3];
            for (int k = 0; k < 3; k++) {
                int kind = k;
                SyncBox s = new SyncBox();
                RwBox rw = new RwBox();
                StampedBox st = new StampedBox();
                AtomicLong inconsistent = new AtomicLong();
                long nanos = runThreads(threads, id -> {
                    ThreadLocalRandom r = ThreadLocalRandom.current();
                    for (int i = 0; i < ops; i++) {
                        if (r.nextInt(100) < readPercent) {
                            long v = kind == 0 ? s.read() : kind == 1 ? rw.read() : st.read();
                            if ((v & 1) != 0) inconsistent.incrementAndGet(); // a と b は常に同じ値なので、合計は偶数のはず
                        } else {
                            long v = r.nextInt(1000);
                            if (kind == 0) s.write(v); else if (kind == 1) rw.write(v); else st.write(v);
                        }
                    }
                });
                if (inconsistent.get() != 0) throw new IllegalStateException("不整合を観測した");
                cells[k] = mops((long) threads * ops, nanos);
            }
            print("  %-12s %-16s %-24s %-14s%n", readPercent + "%", cells[0], cells[1], cells[2]);
        }
    }

    // ===== 4. ロックを持ったまま、外部のコード(コールバック)を呼ぶ =====
    interface Listener { void onChange(int value); }

    static class Counter {
        private final List<Listener> listeners = new ArrayList<>();
        private int value;
        synchronized void addListener(Listener l) { listeners.add(l); }
        synchronized int get() { return value; }

        // よくない例: ロックを持ったまま、リスナーを呼ぶ
        synchronized void incrementBad() {
            value++;
            for (Listener l : listeners) l.onChange(value);
        }

        // 直した例: ロックの中では状態の更新とコピーだけを行い、リスナーはロックの外で呼ぶ
        void incrementGood() {
            List<Listener> copy;
            int v;
            synchronized (this) {
                v = ++value;
                copy = new ArrayList<>(listeners);
            }
            for (Listener l : copy) l.onChange(v);
        }
    }

    static boolean callbackDeadlocks(boolean good) throws Exception {
        Counter counter = new Counter();
        Object other = new Object();
        CountDownLatch inCallback = new CountDownLatch(1), otherHeld = new CountDownLatch(1);
        // リスナーは、別のロック(other)を取る。別のスレッドは、other を持ったまま counter.get() を呼ぶ
        counter.addListener(v -> {
            inCallback.countDown();
            try { otherHeld.await(); } catch (InterruptedException e) { return; }
            synchronized (other) { }
        });
        Thread a = new Thread(() -> { if (good) counter.incrementGood(); else counter.incrementBad(); });
        Thread b = new Thread(() -> {
            try { inCallback.await(); } catch (InterruptedException e) { return; }
            synchronized (other) {
                otherHeld.countDown();
                counter.get();
            }
        });
        a.setDaemon(true); b.setDaemon(true);
        a.start(); b.start();
        a.join(2000); b.join(2000);
        return a.isAlive() || b.isAlive();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 不変条件(size == count)を、別々のロックで守る / 1つのロックで守る");
        invariantLock();
        quiet = true; granularity(); readWrite(); quiet = false; // ウォームアップ
        System.out.println("## 2. ロックの粒度(" + KEYS + " 個のカウンターを、スレッドごとに" + OPS_PER_THREAD + " 回更新)");
        granularity();
        System.out.println("## 3. 読み取りと書き込みの割合(8スレッド、各" + 2_000_000 + " 回)");
        readWrite();
        System.out.println("## 4. ロックを持ったままコールバックを呼ぶ(2秒待って、止まっていたら デッドロック)");
        System.out.println("  ロックの中でコールバック: " + (callbackDeadlocks(false) ? "デッドロックした" : "完了した"));
        System.out.println("  ロックの外でコールバック: " + (callbackDeadlocks(true) ? "デッドロックした" : "完了した"));
        System.exit(0);
    }
}
