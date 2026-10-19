// 実行: java ShareNothing.java
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ShareNothing {
    static void runThreads(int threads, Runnable body) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                body.run();
            });
            t.start();
            list.add(t);
        }
        start.countDown();
        for (Thread t : list) t.join();
    }

    // ===== 1. スレッドセーフでないオブジェクトを共有する / スレッドごとに持つ / 不変なものに替える =====
    static final SimpleDateFormat SHARED = new SimpleDateFormat("yyyy-MM-dd");
    static final ThreadLocal<SimpleDateFormat> PER_THREAD = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    static final DateTimeFormatter IMMUTABLE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static void formatters() throws Exception {
        String[] kinds = {"SimpleDateFormat を共有", "SimpleDateFormat をスレッドごとに持つ", "DateTimeFormatter(不変)を共有"};
        for (String kind : kinds) {
            AtomicInteger wrong = new AtomicInteger(), exceptions = new AtomicInteger();
            runThreads(8, () -> {
                for (int i = 0; i < 20_000; i++) {
                    String text = String.format("2026-%02d-%02d", 1 + i % 12, 1 + i % 28);
                    try {
                        String parsed;
                        if (kind.startsWith("SimpleDateFormat を共有")) {
                            parsed = SHARED.format(SHARED.parse(text));
                        } else if (kind.contains("スレッドごと")) {
                            SimpleDateFormat f = PER_THREAD.get();
                            parsed = f.format(f.parse(text));
                        } else {
                            parsed = LocalDate.parse(text, IMMUTABLE).format(IMMUTABLE);
                        }
                        if (!parsed.equals(text)) wrong.incrementAndGet();
                    } catch (ParseException | RuntimeException e) {
                        exceptions.incrementAndGet();
                    }
                }
            });
            System.out.printf("  %-38s 160000 回のうち、結果が違った回数 %d、例外 %d%n", kind, wrong.get(), exceptions.get());
        }
    }

    // ===== 2. スレッドプールで ThreadLocal を使う: 前のタスクの値が残る =====
    static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    static void threadLocalLeftover() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1); // 1本のスレッドを使い回す
        pool.submit(() -> CURRENT_USER.set("alice")).get();      // リクエスト1: alice としてログインして、片付けをしない
        String seen = pool.submit(() -> CURRENT_USER.get()).get(); // リクエスト2: 何も設定していない
        System.out.println("  片付けなし: 別のタスクが読んだ CURRENT_USER = " + seen);
        pool.submit(() -> { try { CURRENT_USER.set("alice"); } finally { CURRENT_USER.remove(); } }).get();
        String afterRemove = pool.submit(() -> CURRENT_USER.get()).get();
        System.out.println("  remove() あり: 別のタスクが読んだ CURRENT_USER = " + afterRemove);
        pool.shutdown();
    }

    // ===== 3. 範囲を閉じる(try-finally)と、InheritableThreadLocal が引き継ぐ値 =====
    // 値を設定して、必ず片付ける。ThreadLocal を「範囲つき」で使うための小さな関数
    static void runWithUser(String user, Runnable body) {
        CURRENT_USER.set(user);
        try {
            body.run();
        } finally {
            CURRENT_USER.remove();
        }
    }

    static final InheritableThreadLocal<String> INHERITED_USER = new InheritableThreadLocal<>();

    static void scopedAndInherited() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> runWithUser("alice", () -> {
            System.out.println("  runWithUser の中: " + CURRENT_USER.get());
        })).get();
        String after = pool.submit(() -> CURRENT_USER.get()).get();
        System.out.println("  同じスレッドの、runWithUser の外: " + after);
        try {
            pool.submit(() -> runWithUser("bob", () -> { throw new IllegalStateException("途中で失敗"); })).get();
        } catch (java.util.concurrent.ExecutionException e) {
            // 例外で抜けても、finally で片付く
        }
        System.out.println("  例外で抜けた後の、同じスレッド: " + pool.submit(() -> CURRENT_USER.get()).get());
        pool.shutdown();

        // InheritableThreadLocal: 子のスレッドは、作られた時点の親の値を引き継ぐ
        INHERITED_USER.set("alice");
        ExecutorService inheritedPool = Executors.newFixedThreadPool(1);
        String first = inheritedPool.submit(() -> INHERITED_USER.get()).get(); // このとき、プールのスレッドが作られる
        INHERITED_USER.set("bob");                                              // 親は、あとから値を変える
        String second = inheritedPool.submit(() -> INHERITED_USER.get()).get(); // 作り済みのスレッドを使い回す
        String[] fresh = new String[1];
        Thread t = new Thread(() -> fresh[0] = INHERITED_USER.get());          // 変えたあとに作った新しいスレッド
        t.start(); t.join();
        System.out.println("  親が alice を設定し、プールのスレッドが作られたとき: " + first);
        System.out.println("  親が bob に変えたあと、同じプールのスレッドが読んだ値: " + second);
        System.out.println("  親が bob に変えたあとに作った新しいスレッドが読んだ値: " + fresh[0]);
        inheritedPool.shutdown();
    }

    // ===== 4. 共有変数で渡す / キューで渡す =====
    static volatile int latest = -1;

    static void handOff() throws Exception {
        int items = 1_000_000;
        AtomicInteger receivedShared = new AtomicInteger();
        Thread producer = new Thread(() -> { for (int i = 0; i < items; i++) latest = i; });
        Thread consumer = new Thread(() -> {
            int last = -1;
            while (last < items - 1) {
                int v = latest;           // 共有変数の「最新値」を読む
                if (v != last) { receivedShared.incrementAndGet(); last = v; }
            }
        });
        consumer.start(); producer.start(); producer.join(); consumer.join();
        System.out.println("  共有変数(volatile)で渡す: " + items + " 個のうち受け取れた個数 " + receivedShared.get());

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(1024);
        AtomicInteger receivedQueue = new AtomicInteger();
        AtomicInteger outOfOrder = new AtomicInteger();
        Thread qProducer = new Thread(() -> {
            try { for (int i = 0; i < items; i++) queue.put(i); } catch (InterruptedException ignored) { }
        });
        Thread qConsumer = new Thread(() -> {
            try {
                int expected = 0;
                for (int i = 0; i < items; i++) {
                    int v = queue.take();
                    if (v != expected++) outOfOrder.incrementAndGet();
                    receivedQueue.incrementAndGet();
                }
            } catch (InterruptedException ignored) { }
        });
        qConsumer.start(); qProducer.start(); qProducer.join(); qConsumer.join();
        System.out.println("  BlockingQueue で渡す      : " + items + " 個のうち受け取れた個数 " + receivedQueue.get() + "、順番が違った個数 " + outOfOrder.get());
    }

    // ===== 5. 書き込み時にコピーする: CopyOnWriteArrayList の書き込みコスト =====
    static void copyOnWrite() {
        int n = 50_000;
        for (int round = 0; round < 3; round++) {
            List<Integer> sync = Collections.synchronizedList(new ArrayList<>());
            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) sync.add(i);
            long tSync = System.nanoTime() - t0;
            List<Integer> cow = new CopyOnWriteArrayList<>();
            long t1 = System.nanoTime();
            for (int i = 0; i < n; i++) cow.add(i);
            long tCow = System.nanoTime() - t1;
            if (round == 2) System.out.printf("  末尾に %d 件追加: synchronizedList %.1f ミリ秒、CopyOnWriteArrayList %.1f ミリ秒%n", n, tSync / 1e6, tCow / 1e6);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. スレッドセーフでないオブジェクトを共有する(8スレッド × 20000 回)");
        formatters();
        System.out.println("## 2. スレッドプールで ThreadLocal を使う");
        threadLocalLeftover();
        System.out.println("## 3. 範囲を閉じる関数と、InheritableThreadLocal");
        scopedAndInherited();
        System.out.println("## 4. データを共有変数で渡す場合と、キューで渡す場合");
        handOff();
        System.out.println("## 5. CopyOnWriteArrayList");
        copyOnWrite();
    }
}
