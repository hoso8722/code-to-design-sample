// 実行: mvn -q compile exec:java -Dexec.mainClass=PoolSizing
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PoolSizing {
    static final int QUERIES = 160;
    static final int APP_THREADS = 32; // アプリケーション側の同時処理数は、プールより多くしておく
    // データベースのCPUを使う検索(接続を待つ時間ではなく、データベース側の処理が主になる)
    static final String SQL = "SELECT sum(i) FROM generate_series(1, 500000) AS t(i)";

    static long run(int poolSize) throws Exception {
        try (HikariDataSource pool = Pool.create(poolSize, 300_000)) {
            ExecutorService executor = Executors.newFixedThreadPool(APP_THREADS);
            try {
                long start = System.nanoTime();
                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < QUERIES; i++) {
                    futures.add(executor.submit(() -> {
                        try (Connection connection = pool.getConnection();
                             PreparedStatement statement = connection.prepareStatement(SQL);
                             ResultSet results = statement.executeQuery()) {
                            results.next();
                        }
                        return null;
                    }));
                }
                for (Future<?> future : futures) {
                    future.get();
                }
                return (System.nanoTime() - start) / 1_000_000;
            } finally {
                executor.shutdown();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("検索 " + QUERIES + " 回、アプリケーションの同時処理 " + APP_THREADS
                + "、利用可能なCPU " + Runtime.getRuntime().availableProcessors());
        System.out.println("プールの大きさ,所要時間ms,1秒あたりの検索数");
        run(4); // ウォームアップ
        for (int poolSize : new int[] {1, 2, 4, 8, 16, 32}) {
            long millis = run(poolSize);
            System.out.printf("%d,%d,%.1f%n", poolSize, millis, QUERIES * 1000.0 / millis);
        }
    }
}
