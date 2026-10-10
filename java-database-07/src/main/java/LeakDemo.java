// 実行: mvn -q compile exec:java -Dexec.mainClass=LeakDemo
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class LeakDemo {
    // 接続を借りて、閉じずに戻る(リーク)
    static void leak(HikariDataSource pool) throws SQLException {
        Connection connection = pool.getConnection();
        connection.createStatement().close();
        // close を呼ばない
    }

    // try-with-resourcesで借りて、必ず返す
    static void useAndReturn(HikariDataSource pool) throws SQLException {
        try (Connection connection = pool.getConnection()) {
            connection.createStatement().close();
        }
    }

    public static void main(String[] args) throws Exception {
        try (HikariDataSource pool = Pool.create(2, 1000)) {
            for (int i = 1; i <= 5; i++) {
                useAndReturn(pool);
            }
            System.out.println("返す場合 : 5回借りても問題なし。使用中=" + pool.getHikariPoolMXBean().getActiveConnections());
        }

        try (HikariDataSource pool = Pool.create(2, 1000)) {
            for (int i = 1; i <= 3; i++) {
                long start = System.nanoTime();
                try {
                    leak(pool);
                    System.out.println("返さない場合 " + i + "回目: 借りられた。使用中=" + pool.getHikariPoolMXBean().getActiveConnections());
                } catch (SQLException e) {
                    long waited = (System.nanoTime() - start) / 1_000_000;
                    System.out.println("返さない場合 " + i + "回目: " + e.getClass().getSimpleName() + "(約" + waited + "ms 待って失敗)");
                    System.out.println("  " + e.getMessage());
                }
            }
        }
    }
}
