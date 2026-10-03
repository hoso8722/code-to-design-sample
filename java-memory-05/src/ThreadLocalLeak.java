// 実行: java ThreadLocalLeak.java noremove
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ThreadLocalLeak {
    // リクエストごとの作業用バッファ（4MB）をスレッドに持たせる
    static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<>();
    // リクエストを処理しているユーザー名をスレッドに持たせる
    static final ThreadLocal<String> USER = new ThreadLocal<>();

    static long usedAfterGcMB() {
        System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public static void main(String[] args) throws Exception {
        boolean remove = args.length > 0 && args[0].equals("remove");
        System.out.println("すべて処理する前: " + usedAfterGcMB() + " MB");

        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String user = "user-" + i;
            tasks.add(pool.submit(() -> {
                try {
                    BUFFER.set(new byte[4 * 1024 * 1024]);
                    USER.set(user);
                    // ... BUFFER と USER を使ってリクエストを処理する ...
                } finally {
                    if (remove) {
                        BUFFER.remove();
                        USER.remove();
                    }
                }
            }));
        }
        for (Future<?> task : tasks) task.get();  // すべて処理し終わるのを待つ（スレッドプールは終了しない）
        System.out.println("すべて処理した後: " + usedAfterGcMB() + " MB");

        // 別の処理が、同じスレッドで USER を読んでみる
        pool.submit(() -> System.out.println("次のタスクから見た USER: " + USER.get())).get();

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}
