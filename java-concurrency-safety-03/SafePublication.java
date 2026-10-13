// 実行: java SafePublication.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class SafePublication {
    // ===== 1. this の漏れ: コンストラクタが終わる前に、自分を外に渡す =====
    static final List<Object> registry = new CopyOnWriteArrayList<>();

    static class Leaky {
        final int value; // final でも、this が漏れると保証されない
        Leaky(CountDownLatch registered, CountDownLatch mayFinish) throws InterruptedException {
            registry.add(this);          // コンストラクタの途中で、自分を登録する
            registered.countDown();
            mayFinish.await();           // 別のスレッドが読む時間を作る(本物のコードでは、ほかの初期化処理にあたる)
            this.value = 42;
        }
    }

    static class Safe {
        final int value;
        private Safe() { this.value = 42; }
        static Safe create() {           // 作り終わってから登録する
            Safe s = new Safe();
            registry.add(s);
            return s;
        }
    }

    static void thisEscape() throws Exception {
        registry.clear();
        CountDownLatch registered = new CountDownLatch(1), mayFinish = new CountDownLatch(1);
        Thread constructing = new Thread(() -> {
            try {
                new Leaky(registered, mayFinish);
            } catch (InterruptedException ignored) { }
        });
        constructing.start();
        registered.await();
        Leaky seen = (Leaky) registry.get(0);
        System.out.println("  this を漏らしたコンストラクタの途中で読んだ final フィールド: " + seen.value + "(コンストラクタ完了後は 42)");
        mayFinish.countDown();
        constructing.join();
        System.out.println("  コンストラクタ完了後: " + seen.value);
        registry.clear();
        Safe.create();
        System.out.println("  作り終わってから登録した場合に、registry から読んだ値: " + ((Safe) registry.get(0)).value);
    }

    // ===== 2. 安全でない公開: 参照を通常のフィールドで渡す =====
    static final int N = 2_000_000;

    static class Holder {
        final int x;
        int y;
        Holder() { x = 3; y = 4; }
    }

    static int[] unsafePublish(boolean useVolatile) throws Exception {
        Holder[] plain = new Holder[N];
        AtomicIntegerArray go1 = new AtomicIntegerArray(N), go2 = new AtomicIntegerArray(N);
        Holder[] viaVolatile = new Holder[N];
        java.lang.invoke.VarHandle vh = java.lang.invoke.MethodHandles.arrayElementVarHandle(Holder[].class);
        int[] seenX = new int[N], seenY = new int[N];
        Thread writer = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                go1.set(i, 1);
                while (go2.get(i) == 0) { }
                if (useVolatile) vh.setVolatile(viaVolatile, i, new Holder());
                else plain[i] = new Holder();
            }
        });
        Thread reader = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                go2.set(i, 1);
                while (go1.get(i) == 0) { }
                Holder h = useVolatile ? (Holder) vh.getVolatile(viaVolatile, i) : plain[i];
                if (h != null) { seenX[i] = h.x; seenY[i] = h.y; } else { seenX[i] = -1; seenY[i] = -1; }
            }
        });
        writer.start(); reader.start(); writer.join(); reader.join();
        int notNull = 0, xWrong = 0, yWrong = 0;
        for (int i = 0; i < N; i++) {
            if (seenX[i] == -1) continue;
            notNull++;
            if (seenX[i] != 3) xWrong++;
            if (seenY[i] != 4) yWrong++;
        }
        return new int[] {notNull, xWrong, yWrong};
    }

    // ===== 3. ダブルチェックドロッキング =====
    static class Resource {
        int value = 42; // final にすると、JLS 17.5 により初期化済みの値が見える。ここでは、わざと final にしない
    }

    static class LazyPlain { // volatile なし
        private Resource instance;
        Resource get() {
            if (instance == null) {
                synchronized (this) {
                    if (instance == null) instance = new Resource();
                }
            }
            return instance;
        }
    }

    static class LazyVolatile {
        private volatile Resource instance;
        Resource get() {
            Resource local = instance;
            if (local == null) {
                synchronized (this) {
                    local = instance;
                    if (local == null) instance = local = new Resource();
                }
            }
            return local;
        }
    }

    static class LazyHolder { // 初期化をクラスのロードに任せる(ホルダーイディオム)
        private static class Holder {
            static final Resource INSTANCE = new Resource();
        }
        static Resource get() { return Holder.INSTANCE; }
    }

    static int doubleChecked(boolean useVolatile) throws Exception {
        int rounds = 300_000;
        LazyPlain[] plains = new LazyPlain[rounds];
        LazyVolatile[] volatiles = new LazyVolatile[rounds];
        for (int i = 0; i < rounds; i++) { plains[i] = new LazyPlain(); volatiles[i] = new LazyVolatile(); }
        AtomicIntegerArray go1 = new AtomicIntegerArray(rounds), go2 = new AtomicIntegerArray(rounds);
        AtomicInteger wrong = new AtomicInteger();
        Runnable[] tasks = new Runnable[2];
        for (int t = 0; t < 2; t++) {
            AtomicIntegerArray mine = t == 0 ? go1 : go2, other = t == 0 ? go2 : go1;
            tasks[t] = () -> {
                for (int i = 0; i < rounds; i++) {
                    mine.set(i, 1);
                    while (other.get(i) == 0) { }
                    Resource r = useVolatile ? volatiles[i].get() : plains[i].get();
                    if (r == null || r.value != 42) wrong.incrementAndGet();
                }
            };
        }
        Thread a = new Thread(tasks[0]), b = new Thread(tasks[1]);
        a.start(); b.start(); a.join(); b.join();
        return wrong.get();
    }

    // ===== 4. 不変条件を持つ値を、可変にするか、不変にするか =====
    static class MutableRange { // 常に high - low == 1 のつもり
        volatile int low = 0, high = 1;
    }

    record Range(int low, int high) {
        Range {
            if (high - low != 1) throw new IllegalArgumentException("high - low は 1 でなければならない");
        }
    }

    static void rangeInvariant() throws Exception {
        for (String kind : new String[] {"可変(setterで2回に分けて更新)", "不変(recordを差し替える)"}) {
            MutableRange mutable = new MutableRange();
            AtomicReference<Range> immutable = new AtomicReference<>(new Range(0, 1));
            AtomicInteger observations = new AtomicInteger(), broken = new AtomicInteger();
            Thread updater = new Thread(() -> {
                for (int i = 0; i < 3_000_000; i++) {
                    if (kind.startsWith("可変")) { mutable.high = i + 2; mutable.low = i + 1; }
                    else immutable.set(new Range(i + 1, i + 2));
                }
            });
            Thread observer = new Thread(() -> {
                while (updater.isAlive() || observations.get() == 0) {
                    int low, high;
                    if (kind.startsWith("可変")) { low = mutable.low; high = mutable.high; }
                    else { Range r = immutable.get(); low = r.low(); high = r.high(); }
                    observations.incrementAndGet();
                    if (high - low != 1) broken.incrementAndGet();
                }
            });
            updater.start(); observer.start(); updater.join(); observer.join();
            System.out.printf("  %-34s 観測 %d 回のうち、high - low が 1 でなかった回数 %d%n", kind, observations.get(), broken.get());
        }
    }

    // ===== 5. 防御的コピー: record でも、中身の List が可変なら不変ではない =====
    record SharedTeam(String name, List<String> members) {}

    record CopiedTeam(String name, List<String> members) {
        CopiedTeam {
            members = List.copyOf(members); // 渡されたリストと切り離す(以後は変更できない)
        }
    }

    static void defensiveCopy() {
        List<String> source = new ArrayList<>(List.of("alice", "bob"));
        SharedTeam shared = new SharedTeam("A", source);
        CopiedTeam copied = new CopiedTeam("A", source);
        source.add("mallory"); // 作ったあとに、元のリストを変更する
        System.out.println("  recordをそのまま使った場合 : " + shared.members());
        System.out.println("  List.copyOf で切り離した場合: " + copied.members());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. this の漏れ");
        thisEscape();

        System.out.println("## 2. 安全でない公開(" + N + " ラウンド): x は final、y は通常のフィールド");
        int[] plain = unsafePublish(false);
        System.out.printf("  通常の参照   : 参照が見えた %d 回のうち、x が期待値でなかった回数 %d、y が期待値でなかった回数 %d%n", plain[0], plain[1], plain[2]);
        int[] vol = unsafePublish(true);
        System.out.printf("  volatile の参照: 参照が見えた %d 回のうち、x が期待値でなかった回数 %d、y が期待値でなかった回数 %d%n", vol[0], vol[1], vol[2]);

        System.out.println("## 3. ダブルチェックドロッキング(30万ラウンド、2スレッドが同時に get): 未初期化の値を見た回数");
        System.out.println("  volatile なし : " + doubleChecked(false));
        System.out.println("  volatile あり : " + doubleChecked(true));
        System.out.println("  ホルダーイディオム: " + LazyHolder.get().value + "(クラスの初期化に任せるので、常に初期化済み)");

        System.out.println("## 4. 不変条件 high - low == 1");
        rangeInvariant();

        System.out.println("## 5. 防御的コピー");
        defensiveCopy();
    }
}
