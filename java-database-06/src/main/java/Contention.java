// 実行: mvn -q compile exec:java -Dexec.mainClass=Contention
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Contention {
    static final int THREADS = 8;
    static final int OPERATIONS_PER_THREAD = 20;
    static final long WORK_MILLIS = 5;      // 読んでから書くまでの、業務の処理にかかる時間

    record Result(int succeeded, int failed, int retries, long millis) {}

    interface Operation {
        // 成功したら true。再試行の回数を retries に足す
        boolean run(Connection connection, long id, AtomicInteger retries) throws Exception;
    }

    // 楽観ロック。maxAttempts 回まで試し、それでも競合したら諦めて false を返す
    static Operation optimistic(int maxAttempts) {
        return (connection, id, retries) -> {
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                Shop.Row seen = Shop.read(connection, id);
                Thread.sleep(WORK_MILLIS);
                if (Shop.updateIfUnchanged(connection, id, seen, seen.stock() - 1) == 1) {
                    return true;
                }
                retries.incrementAndGet();
            }
            return false;
        };
    }

    static boolean pessimistic(Connection connection, long id, AtomicInteger retries) throws Exception {
        connection.setAutoCommit(false);
        try {
            int stock;
            try (PreparedStatement statement = connection.prepareStatement("SELECT stock FROM products WHERE id = ? FOR UPDATE")) {
                statement.setLong(1, id);
                try (ResultSet results = statement.executeQuery()) {
                    results.next();
                    stock = results.getInt(1);
                }
            }
            Thread.sleep(WORK_MILLIS);
            try (PreparedStatement statement = connection.prepareStatement("UPDATE products SET stock = ? WHERE id = ?")) {
                statement.setInt(1, stock - 1);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // sameRow が true なら全員が同じ行(競合が多い)、false ならスレッドごとに別の行(競合が少ない)
    static Result run(Operation operation, boolean sameRow) throws Exception {
        try (Connection setup = Db.connect()) {
            Shop.reset(setup, THREADS, 1000);
        }
        AtomicInteger retries = new AtomicInteger();
        AtomicInteger succeeded = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        long start = System.nanoTime();
        try {
            java.util.List<Future<?>> futures = new java.util.ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                long id = sameRow ? 1 : t + 1;
                futures.add(pool.submit(() -> {
                    try (Connection connection = Db.connect()) {
                        for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                            if (operation.run(connection, id, retries)) {
                                succeeded.incrementAndGet();
                            }
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdown();
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        int total = THREADS * OPERATIONS_PER_THREAD;
        // 在庫の減った合計が、成功した回数と一致するか(更新が失われていないか)
        try (Connection check = Db.connect(); PreparedStatement statement = check.prepareStatement("SELECT sum(1000 - stock) FROM products");
             ResultSet results = statement.executeQuery()) {
            results.next();
            int reduced = results.getInt(1);
            if (reduced != succeeded.get()) {
                throw new IllegalStateException("更新が失われた: 成功 " + succeeded.get() + " 回、在庫の減少 " + reduced);
            }
        }
        return new Result(succeeded.get(), total - succeeded.get(), retries.get(), millis);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("スレッド " + THREADS + " × 各 " + OPERATIONS_PER_THREAD + " 回の在庫更新、1回あたり業務の処理 "
                + WORK_MILLIS + " ms");
        System.out.println("方式,競合,成功,失敗,再試行,所要時間ms");
        record Case(String name, Operation operation, boolean sameRow) {}
        java.util.List<Case> cases = java.util.List.of(
                new Case("楽観ロック", optimistic(1000), false),
                new Case("悲観ロック", Contention::pessimistic, false),
                new Case("楽観ロック", optimistic(1000), true),
                new Case("悲観ロック", Contention::pessimistic, true),
                new Case("楽観ロック(再試行は3回まで)", optimistic(3), true));
        for (Case c : cases) {
            Result r = run(c.operation(), c.sameRow());
            System.out.printf("%s,%s,%d,%d,%d,%d%n", c.name(), c.sameRow() ? "多い(全員が同じ行)" : "少ない(別々の行)",
                    r.succeeded(), r.failed(), r.retries(), r.millis());
        }
    }
}
