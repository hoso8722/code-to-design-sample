// 実行: mvn -q compile exec:java -Dexec.mainClass=PoolExhaustion
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PoolExhaustion {
    public static void main(String[] args) throws Exception {
        int poolSize = 3;
        int tasks = 10;
        try (HikariDataSource pool = Pool.create(poolSize, 10_000)) {
            ExecutorService executor = Executors.newFixedThreadPool(tasks);
            long start = System.nanoTime();
            List<Future<long[]>> futures = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                futures.add(executor.submit(() -> {
                    long asked = System.nanoTime();
                    try (Connection connection = pool.getConnection()) {
                        long waitedMillis = (System.nanoTime() - asked) / 1_000_000; // 接続を借りるまでに待った時間
                        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_sleep(0.3)")) {
                            statement.execute(); // 300ミリ秒かかる処理
                        }
                        return new long[] {waitedMillis};
                    }
                }));
            }
            // 途中の様子:使用中と、接続を待っているスレッドの数
            Thread.sleep(150);
            System.out.println("150ms時点: 使用中 " + pool.getHikariPoolMXBean().getActiveConnections()
                    + " / 空き " + pool.getHikariPoolMXBean().getIdleConnections()
                    + " / 接続待ちのスレッド " + pool.getHikariPoolMXBean().getThreadsAwaitingConnection());

            List<Long> waits = new ArrayList<>();
            for (Future<long[]> future : futures) {
                waits.add(future.get()[0]);
            }
            executor.shutdown();
            java.util.Collections.sort(waits);
            System.out.println("各処理が接続を借りるまでに待った時間(ms、短い順): " + waits);
            System.out.println("全体の所要時間: 約" + (System.nanoTime() - start) / 1_000_000 + " ms("
                    + tasks + "処理 / プール" + poolSize + " / 1処理300ms)");
        }
    }
}
