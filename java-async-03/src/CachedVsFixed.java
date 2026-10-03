// 実行: java CachedVsFixed.java cached
import java.lang.management.ManagementFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CachedVsFixed {
    public static void main(String[] args) throws Exception {
        String kind = args.length > 0 ? args[0] : "cached";
        ExecutorService pool = kind.equals("cached") ? Executors.newCachedThreadPool() : Executors.newFixedThreadPool(16);
        int before = ManagementFactory.getThreadMXBean().getThreadCount();

        long start = System.nanoTime();
        for (int i = 0; i < 2000; i++) {
            pool.submit(() -> {
                Thread.sleep(200);  // 200ミリ秒の待ち（sleep で模擬する）
                return null;
            });
        }
        Thread.sleep(100);  // 投入し終わってから100ミリ秒後の様子を見る
        ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
        System.out.printf("%s: スレッド数 %d → %d、キューで待っているタスク %d 個%n",
                kind, before, ManagementFactory.getThreadMXBean().getThreadCount(), tpe.getQueue().size());

        pool.close();
        System.out.printf("%s: 2000個すべて終わるまで %d ms%n", kind, (System.nanoTime() - start) / 1_000_000);
    }
}
