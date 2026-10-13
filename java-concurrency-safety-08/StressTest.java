// 実行: java StressTest.java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {
    static int plainCount; // 壊れたカウンター(通常の変数への ++)
    static final AtomicInteger atomicCount = new AtomicInteger();

    // 2本のスレッドが、それぞれ increments 回 ++ する。合計が合うかを返す。
    // useLatch: true なら、2本が同時に走り出すように、ラッチで待ち合わせる
    static boolean trial(int increments, boolean atomic, boolean useLatch) throws Exception {
        plainCount = 0;
        atomicCount.set(0);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 2; t++) {
            Thread thread = new Thread(() -> {
                if (useLatch) {
                    try { start.await(); } catch (InterruptedException e) { return; }
                }
                for (int i = 0; i < increments; i++) {
                    if (atomic) atomicCount.incrementAndGet(); else plainCount++;
                }
            });
            threads.add(thread);
        }
        for (Thread thread : threads) thread.start();
        start.countDown();
        for (Thread thread : threads) thread.join();
        return (atomic ? atomicCount.get() : plainCount) == 2 * increments;
    }

    // 別の種類のバグ: containsKey してから put する(check-then-act)。8本のスレッドが、同じ1000個のキーを登録する
    static boolean mapTrial(boolean safe) throws Exception {
        Map<Integer, Integer> map = new ConcurrentHashMap<>();
        AtomicInteger winners = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            int id = t;
            Thread thread = new Thread(() -> {
                try { start.await(); } catch (InterruptedException e) { return; }
                for (int k = 0; k < 1000; k++) {
                    if (safe) {
                        if (map.putIfAbsent(k, id) == null) winners.incrementAndGet();
                    } else if (!map.containsKey(k)) {
                        map.put(k, id);
                        winners.incrementAndGet();
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread thread : threads) thread.join();
        return winners.get() == 1000;
    }

    public static void main(String[] args) throws Exception {
        int trials = 2000;
        for (int warm = 0; warm < 200; warm++) trial(1000, false, true); // ウォームアップ
        System.out.println("  壊れたカウンターを、2スレッド × 「1スレッドあたりの回数」回 ++ して、合計が合わなかった試行の割合(" + trials + " 試行)");
        System.out.printf("  %-10s %-22s %-22s %-22s%n", "回数", "壊れた版(ラッチなし)", "壊れた版(ラッチあり)", "AtomicInteger(ラッチあり)");
        for (int increments : new int[] {10, 100, 1_000, 100_000}) {
            int[] failed = new int[3];
            for (int i = 0; i < trials; i++) {
                if (!trial(increments, false, false)) failed[0]++;
                if (!trial(increments, false, true)) failed[1]++;
                if (!trial(increments, true, true)) failed[2]++;
            }
            System.out.printf("  %-10d %-22s %-22s %-22s%n", increments, failed[0] + "/" + trials, failed[1] + "/" + trials, failed[2] + "/" + trials);
        }
        System.out.println("  check-then-act(8スレッド × 1000キー)で、「最初に登録した」と思ったスレッドの延べ数が1000でなかった試行(" + trials + " 試行)");
        int brokenFailed = 0, safeFailed = 0;
        for (int i = 0; i < trials; i++) {
            if (!mapTrial(false)) brokenFailed++;
            if (!mapTrial(true)) safeFailed++;
        }
        System.out.printf("  containsKey → put: %d/%d、putIfAbsent: %d/%d%n", brokenFailed, trials, safeFailed, trials);
    }
}
