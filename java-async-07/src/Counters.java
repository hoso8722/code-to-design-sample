// 実行: java Counters.java 8
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

public class Counters {
    static final int TOTAL = 16_000_000;  // 全スレッドで合計 1,600万回増やす

    interface Counter {
        void increment();
        long get();
    }

    static class Unsafe implements Counter {  // 何もしない（正しくない）
        long value;
        public void increment() { value++; }
        public long get() { return value; }
    }

    static class Synchronized implements Counter {
        long value;
        public synchronized void increment() { value++; }
        public synchronized long get() { return value; }
    }

    static class Locked implements Counter {
        final ReentrantLock lock = new ReentrantLock();
        long value;
        public void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }
        public long get() { return value; }
    }

    static class Atomic implements Counter {
        final AtomicLong value = new AtomicLong();
        public void increment() { value.incrementAndGet(); }
        public long get() { return value.get(); }
    }

    static class Adder implements Counter {
        final LongAdder value = new LongAdder();
        public void increment() { value.increment(); }
        public long get() { return value.sum(); }
    }

    // threads 本のスレッドで合計 TOTAL 回 increment し、[かかった時間(ms), 結果] を返す
    static long[] run(Counter counter, int threads) throws InterruptedException {
        long start = System.nanoTime();
        List<Thread> list = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < TOTAL / threads; i++) counter.increment();
            });
            thread.start();
            list.add(thread);
        }
        for (Thread thread : list) thread.join();
        return new long[] {(System.nanoTime() - start) / 1_000_000, counter.get()};
    }

    public static void main(String[] args) throws Exception {
        int threads = args.length > 0 ? Integer.parseInt(args[0]) : 8;
        for (String name : List.of("unsafe", "synchronized", "ReentrantLock", "AtomicLong", "LongAdder")) {
            for (int warmup = 0; warmup < 2; warmup++) run(create(name), threads);  // 準備運動
            long[] r = run(create(name), threads);
            System.out.printf("%s,%d,%d,%d%n", name, threads, r[0], r[1]);
        }
    }

    static Counter create(String name) {
        return switch (name) {
            case "unsafe" -> new Unsafe();
            case "synchronized" -> new Synchronized();
            case "ReentrantLock" -> new Locked();
            case "AtomicLong" -> new Atomic();
            default -> new Adder();
        };
    }
}
