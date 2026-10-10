// 実行: mvn -q compile exec:java -Dexec.mainClass=ForUpdateWait
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class ForUpdateWait {
    static volatile long origin = System.nanoTime();

    // t1が行をロックした時点を0とした、経過ミリ秒
    static long now() {
        return (System.nanoTime() - origin) / 1_000_000;
    }

    // 行をロックして、残高を読む
    static void lockRow(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT balance FROM accounts WHERE id = ? FOR UPDATE")) {
            statement.setLong(1, id);
            statement.executeQuery().close();
        }
    }

    static void run(String label, String lockTimeout) throws Exception {
        System.out.println("# " + label);
        try (Connection t1 = Db.connect(); Connection t2 = Db.connect(); Connection reader = Db.connect()) {
            Bank.reset(t1);
            t1.setAutoCommit(false);
            t2.setAutoCommit(false);
            if (lockTimeout != null) {
                try (Statement statement = t2.createStatement()) {
                    statement.execute("SET lock_timeout = '" + lockTimeout + "'");
                }
            }

            origin = System.nanoTime();
            long lockedAt = now();
            lockRow(t1, 1); // t1が行をロックする
            System.out.println("t1,SELECT FOR UPDATEで行をロック," + lockedAt);

            Thread other = new Thread(() -> {
                long issued = now();
                try {
                    Bank.update(t2, 1, new BigDecimal("100.00")); // 同じ行の更新:t1が終わるまで待たされる
                    long returned = now();
                    t2.commit();
                    System.out.println("t2,UPDATEが待たされた," + issued + "," + returned);
                } catch (SQLException e) {
                    System.out.println("t2,UPDATEが失敗(SQLState=" + e.getSQLState() + ")," + issued + "," + now());
                    try {
                        t2.rollback();
                    } catch (SQLException ignored) {
                        // 後始末の失敗は、この実験では扱わない
                    }
                }
            });
            Thread.sleep(200);
            other.start();
            Thread.sleep(100);
            if (lockTimeout == null) {
                // ロック中の行を、通常のSELECTで読む。待たされず、更新前の値が読める
                long before = now();
                BigDecimal seen = Bank.balance(reader, 1);
                System.out.println("reader,通常のSELECTは待たされない 残高=" + seen + "," + before + "," + now());
            }
            Thread.sleep(900);

            long releasedAt = now();
            t1.commit();
            System.out.println("t1,commitしてロックを解放," + lockedAt + "," + releasedAt);
            other.join();
            System.out.println("最終残高 " + Bank.balance(t1, 1));
        }
    }

    public static void main(String[] args) throws Exception {
        run("A. 待たされる(ロック待ちの上限なし)", null);
        run("B. lock_timeout を 300ms にした場合", "300ms");
    }
}
