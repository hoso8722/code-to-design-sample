// 実行: java ConcurrentCollections.java
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentCollections {
    // ===== 1. 弱い一貫性のイテレーター =====
    static void weaklyConsistent() throws Exception {
        int initial = 100_000;
        for (int round = 0; round < 5; round++) {
            Map<Integer, Integer> map = new ConcurrentHashMap<>();
            for (int i = 0; i < initial; i++) map.put(i, i);
            AtomicBoolean stop = new AtomicBoolean();
            Thread writer = new Thread(() -> { // 走査中に、新しいキーを追加し続ける
                for (int i = initial; i < initial * 2 && !stop.get(); i++) map.put(i, i);
            });
            writer.start();
            int count = 0;
            for (Iterator<Map.Entry<Integer, Integer>> it = map.entrySet().iterator(); it.hasNext(); it.next()) count++; // 例外にならない
            stop.set(true);
            writer.join();
            System.out.printf("  %d 回目: 走査で見えた要素数 %d、走査の後の size() %d(走査の開始時は %d)%n", round + 1, count, map.size(), initial);
        }
    }

    // ===== 2. computeIfAbsent の中で、同じマップを更新する =====
    static void recursiveCompute() {
        Map<Integer, Long> memo = new ConcurrentHashMap<>();
        try {
            long result = fib(40, memo);
            System.out.println("  再帰でメモ化した fib(40) = " + result);
        } catch (IllegalStateException e) {
            System.out.println("  再帰でメモ化した fib(40) は例外: " + e.getMessage());
        }
    }

    static long fib(int n, Map<Integer, Long> memo) {
        if (n < 2) return n;
        return memo.computeIfAbsent(n, k -> fib(k - 1, memo) + fib(k - 2, memo)); // 計算の中で、同じマップを更新する
    }

    static long fibSafe(int n, Map<Integer, Long> memo) {
        if (n < 2) return n;
        Long cached = memo.get(n);
        if (cached != null) return cached;
        long value = fibSafe(n - 1, memo) + fibSafe(n - 2, memo);
        memo.putIfAbsent(n, value);
        return value;
    }

    // ===== 3. compute の中で長い処理をする =====
    static void longCompute() throws Exception {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 64; i++) map.put("key" + i, i);
        Thread slow = new Thread(() -> map.compute("key0", (k, v) -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) { return v; }
            return v + 1;
        }));
        slow.start();
        Thread.sleep(100); // slow が compute の中に入るのを待つ
        long t0 = System.nanoTime();
        map.merge("key0", 1, Integer::sum);          // 同じキーを更新する
        long sameKey = System.nanoTime() - t0;
        // 同じ「ビン」(内部のハッシュ表の1つの区画)に入らないキーを探す。ここでは、64個のキーのうち、待たされなかった個数を数える
        int notBlocked = 0;
        for (int i = 1; i < 64; i++) {
            long t1 = System.nanoTime();
            map.merge("key" + i, 1, Integer::sum);
            if ((System.nanoTime() - t1) < 100_000_000L) notBlocked++;
        }
        slow.join();
        System.out.printf("  同じキー(key0)の merge にかかった時間: %d ミリ秒%n", sameKey / 1_000_000);
        System.out.printf("  ほかの63キーのうち、100ミリ秒未満で終わった個数: %d%n", notBlocked);
    }

    // ===== 4. キューの種類 =====
    record Task(String name, long dueNanos) implements Delayed {
        public long getDelay(TimeUnit unit) { return unit.convert(dueNanos - System.nanoTime(), TimeUnit.NANOSECONDS); }
        public int compareTo(Delayed o) { return Long.compare(dueNanos, ((Task) o).dueNanos); }
    }

    static void queueKinds() throws Exception {
        BlockingQueue<Integer> array = new ArrayBlockingQueue<>(2);
        System.out.println("  ArrayBlockingQueue(2): offer を3回 → " + array.offer(1) + ", " + array.offer(2) + ", " + array.offer(3) + "(容量 2)");
        BlockingQueue<Integer> linked = new LinkedBlockingQueue<>();
        System.out.println("  LinkedBlockingQueue(容量を指定しない): remainingCapacity = " + linked.remainingCapacity());
        BlockingQueue<Integer> sync = new SynchronousQueue<>();
        System.out.println("  SynchronousQueue: 受け取る側がいないときの offer → " + sync.offer(1));
        DelayQueue<Task> delay = new DelayQueue<>();
        long now = System.nanoTime();
        delay.put(new Task("B(300ミリ秒後)", now + 300_000_000L));
        delay.put(new Task("A(100ミリ秒後)", now + 100_000_000L));
        System.out.println("  DelayQueue: 直後の poll → " + delay.poll() + "(期限前は取り出せない)");
        System.out.println("  DelayQueue: take の順番 → " + delay.take().name() + ", " + delay.take().name());
    }

    // ===== 5. 容量のないキューは、生産が速いと伸び続ける =====
    static void unbounded() throws Exception {
        for (String kind : new String[] {"LinkedBlockingQueue(容量なし)", "ArrayBlockingQueue(容量 1000)"}) {
            BlockingQueue<byte[]> queue = kind.startsWith("Linked") ? new LinkedBlockingQueue<>() : new ArrayBlockingQueue<>(1000);
            AtomicBoolean stop = new AtomicBoolean();
            Thread consumer = new Thread(() -> { // 1件ごとに1ミリ秒かかる、遅い消費者
                try {
                    while (!stop.get()) { queue.poll(10, TimeUnit.MILLISECONDS); Thread.sleep(1); }
                } catch (InterruptedException ignored) { }
            });
            long[] produced = {0};
            Thread producer = new Thread(() -> { // 速い生産者
                try {
                    while (!stop.get()) { queue.offer(new byte[10_000], 10, TimeUnit.MILLISECONDS); produced[0]++; }
                } catch (InterruptedException ignored) { }
            });
            System.gc();
            consumer.start(); producer.start();
            Thread.sleep(2000);
            int size = queue.size();
            stop.set(true);
            producer.join(); consumer.join();
            System.out.printf("  %-30s 2秒後のキューの長さ %d、生産した件数 %d%n", kind, size, produced[0]);
            queue.clear();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 走査しながら、別のスレッドがキーを追加する(ConcurrentHashMap)");
        weaklyConsistent();
        System.out.println("## 2. computeIfAbsent の中で、同じマップを更新する(再帰)");
        recursiveCompute();
        Map<Integer, Long> memo = new ConcurrentHashMap<>();
        System.out.println("  get → 計算 → putIfAbsent に書き直した fib(40) = " + fibSafe(40, memo));
        System.out.println("## 3. compute の中で、1秒かかる処理をする");
        longCompute();
        System.out.println("## 4. キューの種類");
        queueKinds();
        System.out.println("## 5. 生産が速く、消費が遅いとき");
        unbounded();
    }
}
