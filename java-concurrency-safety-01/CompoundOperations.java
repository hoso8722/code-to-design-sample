// 実行: java CompoundOperations.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class CompoundOperations {
    static final int THREADS = 8;

    // 全スレッドが同時に走り出すように、ラッチで開始をそろえて動かす
    static void runAll(int threads, java.util.function.IntConsumer task) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int id = i;
            Thread t = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    return;
                }
                task.accept(id);
            });
            t.start();
            list.add(t);
        }
        start.countDown();
        for (Thread t : list) t.join();
    }

    // ===== 1. containsKey してから put する(check-then-act) =====
    static void checkThenAct() throws Exception {
        int keys = 100_000;
        for (String name : new String[] {"containsKey → put", "putIfAbsent"}) {
            Map<Integer, Integer> map = new ConcurrentHashMap<>();
            AtomicInteger winners = new AtomicInteger(); // 「自分が最初に登録した」と思ったスレッドの延べ数
            runAll(THREADS, id -> {
                for (int k = 0; k < keys; k++) {
                    if (name.startsWith("containsKey")) {
                        if (!map.containsKey(k)) {
                            map.put(k, id);
                            winners.incrementAndGet();
                        }
                    } else if (map.putIfAbsent(k, id) == null) {
                        winners.incrementAndGet();
                    }
                }
            });
            System.out.printf("  %-20s キー数 %d、「自分が最初」と思った回数 %d(正しくは %d)%n", name, keys, winners.get(), keys);
        }
    }

    // ===== 2. synchronizedList を走査しながら、別のスレッドが追加する =====
    static void iterateWhileModifying() throws Exception {
        for (String name : new String[] {"synchronizedList(走査はロックなし)", "synchronizedList(走査もロックする)", "CopyOnWriteArrayList"}) {
            List<Integer> list = name.startsWith("Copy") ? new CopyOnWriteArrayList<>() : Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < 1000; i++) list.add(i);
            AtomicInteger failures = new AtomicInteger();
            AtomicInteger rounds = new AtomicInteger();
            Thread writer = new Thread(() -> {
                for (int i = 0; i < 2_000_000 && !Thread.currentThread().isInterrupted(); i++) {
                    list.add(i);
                    if (list.size() > 5000) list.remove(0);
                }
            });
            writer.start();
            Thread reader = new Thread(() -> {
                for (int r = 0; r < 2000; r++) {
                    try {
                        if (name.contains("ロックする")) {
                            synchronized (list) {
                                for (Integer x : list) if (x < 0) throw new IllegalStateException();
                            }
                        } else {
                            for (Integer x : list) if (x < 0) throw new IllegalStateException();
                        }
                    } catch (ConcurrentModificationException e) {
                        failures.incrementAndGet();
                    }
                    rounds.incrementAndGet();
                }
            });
            reader.start();
            reader.join();
            writer.interrupt();
            writer.join();
            System.out.printf("  %-34s 走査 %d 回のうち ConcurrentModificationException %d 回%n", name, rounds.get(), failures.get());
        }
    }

    // ===== 3. 2つの値の合計が一定、という不変条件 =====
    record Pair(int a, int b) {}

    static void invariant() throws Exception {
        int total = 1000;
        for (String name : new String[] {"AtomicInteger が2つ", "ロックでまとめる", "不変なPairをCASで差し替える"}) {
            AtomicInteger a = new AtomicInteger(total), b = new AtomicInteger(0);
            ReentrantLock lock = new ReentrantLock();
            int[] plainA = {total}, plainB = {0};
            AtomicReference<Pair> pair = new AtomicReference<>(new Pair(total, 0));
            AtomicInteger observations = new AtomicInteger(), broken = new AtomicInteger();
            AtomicBoolean finished = new AtomicBoolean();
            Thread mover = new Thread(() -> {
                for (int i = 0; i < 3_000_000; i++) {
                    switch (name.charAt(0)) {
                        case 'A' -> { // 1つ減らしてから、もう1つ増やす。それぞれはアトミック
                            if (i % 2 == 0) { a.decrementAndGet(); b.incrementAndGet(); }
                            else { b.decrementAndGet(); a.incrementAndGet(); }
                        }
                        case 'ロ' -> {
                            lock.lock();
                            try {
                                if (i % 2 == 0) { plainA[0]--; plainB[0]++; }
                                else { plainB[0]--; plainA[0]++; }
                            } finally {
                                lock.unlock();
                            }
                        }
                        default -> {
                            int delta = i % 2 == 0 ? 1 : -1;
                            pair.updateAndGet(p -> new Pair(p.a() - delta, p.b() + delta));
                        }
                    }
                }
                finished.set(true);
            });
            Thread observer = new Thread(() -> {
                while (!finished.get()) {
                    int sum;
                    switch (name.charAt(0)) {
                        case 'A' -> sum = a.get() + b.get();
                        case 'ロ' -> {
                            lock.lock();
                            try {
                                sum = plainA[0] + plainB[0];
                            } finally {
                                lock.unlock();
                            }
                        }
                        default -> {
                            Pair p = pair.get();
                            sum = p.a() + p.b();
                        }
                    }
                    observations.incrementAndGet();
                    if (sum != total) broken.incrementAndGet();
                }
            });
            observer.start();
            mover.start();
            mover.join();
            observer.join();
            System.out.printf("  %-28s 観測 %d 回のうち、合計が %d でなかった回数 %d%n", name, observations.get(), total, broken.get());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. check-then-act: " + THREADS + " スレッドが、同じキーを先に登録しようとする");
        checkThenAct();
        System.out.println("## 2. 走査中の変更");
        iterateWhileModifying();
        System.out.println("## 3. 不変条件(a + b = 1000)を、別のスレッドが観測する");
        invariant();
    }
}
