// 実行: java MemoryModelDemo.java
import java.util.concurrent.atomic.AtomicIntegerArray;

public class MemoryModelDemo {
    // ===== 1. 書いてから読む(Dekker型): 並べ替えが見える例 =====
    //   スレッド1: x = 1; r1 = y;      スレッド2: y = 1; r2 = x;
    // 実行が1つの順番に並ぶだけ(逐次一貫)なら、r1 == 0 && r2 == 0 にはならない
    static final int N = 2_000_000;

    static int storeLoad(boolean useVolatile) throws Exception {
        int[] x = new int[N], y = new int[N], r1 = new int[N], r2 = new int[N];
        AtomicIntegerArray vx = new AtomicIntegerArray(N), vy = new AtomicIntegerArray(N);
        AtomicIntegerArray ready1 = new AtomicIntegerArray(N), ready2 = new AtomicIntegerArray(N); // ラウンドごとの待ち合わせ
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                ready1.set(i, 1);
                while (ready2.get(i) == 0) { /* 相手を待つ */ }
                if (useVolatile) { vx.set(i, 1); r1[i] = vy.get(i); }
                else { x[i] = 1; r1[i] = y[i]; }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                ready2.set(i, 1);
                while (ready1.get(i) == 0) { /* 相手を待つ */ }
                if (useVolatile) { vy.set(i, 1); r2[i] = vx.get(i); }
                else { y[i] = 1; r2[i] = x[i]; }
            }
        });
        t1.start(); t2.start(); t1.join(); t2.join();
        int both0 = 0;
        for (int i = 0; i < N; i++) if (r1[i] == 0 && r2[i] == 0) both0++;
        return both0;
    }

    // ===== 2. フラグの可視性: 別のスレッドが書いた値が、ループから見えるか =====
    static boolean plainStop;
    static volatile boolean volatileStop;
    static long sink;

    // 戻り値: ループが止まったか(2秒待って判定)
    static boolean stops(String kind) throws Exception {
        plainStop = false;
        volatileStop = false;
        Thread worker = new Thread(() -> {
            long n = 0;
            switch (kind) {
                case "volatile" -> { while (!volatileStop) n++; }
                case "通常の変数" -> { while (!plainStop) n++; }
                default -> { while (!plainStop) { n++; if ((n & 0xFFFFF) == 0) System.getProperty("user.dir"); } }
            }
            sink = n;
        });
        worker.setDaemon(true);
        worker.start();
        Thread.sleep(300); // ループがJITコンパイルされるまで待つ
        plainStop = true;
        volatileStop = true;
        worker.join(2000);
        return !worker.isAlive();
    }

    // ===== 3. メッセージ渡し: data を書いてから ready を書く。読む側は ready を読んでから data を読む =====
    static int messagePassing(boolean useVolatile) throws Exception {
        int[] data = new int[N], ready = new int[N], rr = new int[N], rd = new int[N];
        AtomicIntegerArray vdata = new AtomicIntegerArray(N), vready = new AtomicIntegerArray(N);
        AtomicIntegerArray go1 = new AtomicIntegerArray(N), go2 = new AtomicIntegerArray(N);
        Thread writer = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                go1.set(i, 1);
                while (go2.get(i) == 0) { }
                if (useVolatile) { vdata.set(i, 42); vready.set(i, 1); }
                else { data[i] = 42; ready[i] = 1; }
            }
        });
        Thread reader = new Thread(() -> {
            for (int i = 0; i < N; i++) {
                go2.set(i, 1);
                while (go1.get(i) == 0) { }
                if (useVolatile) { rr[i] = vready.get(i); rd[i] = vdata.get(i); }
                else { rr[i] = ready[i]; rd[i] = data[i]; }
            }
        });
        writer.start(); reader.start(); writer.join(); reader.join();
        int anomalies = 0;
        for (int i = 0; i < N; i++) if (rr[i] == 1 && rd[i] == 0) anomalies++;
        return anomalies;
    }

    // ===== 4. Thread.start と join による happens-before =====
    static int plainValue;

    static void startJoin() throws Exception {
        plainValue = 1;                       // start() より前の書き込み
        Thread t = new Thread(() -> plainValue = plainValue + 41); // start() 後に見える、join() 前に書く
        t.start();
        t.join();
        System.out.println("  start 前に 1 を書き、スレッド内で +41 して、join 後に読む: " + plainValue + "(常に 42。start と join が happens-before を作る)");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 書いてから読む(Dekker型): r1 == 0 && r2 == 0 になった回数 / " + N + " ラウンド");
        System.out.println("  通常の変数        : " + storeLoad(false));
        System.out.println("  volatile(AtomicIntegerArray): " + storeLoad(true));

        System.out.println("## 2. フラグの可視性: メインが300ミリ秒後にフラグを立て、ループが2秒以内に止まったか(各5回)");
        for (String kind : new String[] {"通常の変数", "通常の変数 + ループ内で他の呼び出し", "volatile"}) {
            int stopped = 0;
            for (int i = 0; i < 5; i++) if (stops(kind)) stopped++;
            System.out.printf("  %-34s 止まった %d / 5 回%n", kind, stopped);
        }

        System.out.println("## 3. メッセージ渡し: ready を見たのに data が 0 だった回数 / " + N + " ラウンド");
        System.out.println("  通常の変数        : " + messagePassing(false));
        System.out.println("  volatile          : " + messagePassing(true));

        System.out.println("## 4. Thread.start と join");
        startJoin();
        System.exit(0); // 止まらないループのスレッドが残っていても終了する
    }
}
