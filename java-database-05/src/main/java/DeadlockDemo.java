// 実行: mvn -q compile exec:java -Dexec.mainClass=DeadlockDemo
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeadlockDemo {
    // from から to へ送金する。先に from の行をロックし、あとで to の行をロックする
    static void transfer(Connection connection, long from, long to, BigDecimal amount, CyclicBarrier barrier)
            throws SQLException {
        connection.setAutoCommit(false);
        try {
            ForUpdateWait.lockRow(connection, from);
            if (barrier != null) {
                // 2つのトランザクションが、1つ目のロックを取り終えるまで待ち合わせる(重なりを確実にする実験用の仕掛け)
                try {
                    barrier.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                    throw new IllegalStateException(e);
                }
            }
            ForUpdateWait.lockRow(connection, to);
            Bank.update(connection, from, amount.negate());
            Bank.update(connection, to, amount);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection setup = Db.connect(); Connection t1 = Db.connect(); Connection t2 = Db.connect()) {
            Bank.reset(setup);
            try (var statement = setup.createStatement(); var results = statement.executeQuery("SHOW deadlock_timeout")) {
                results.next();
                System.out.println("deadlock_timeout: " + results.getString(1));
            }
            CyclicBarrier barrier = new CyclicBarrier(2);
            String[] results = new String[2];
            long start = System.nanoTime();

            Thread a = new Thread(() -> results[0] = attempt(t1, 1, 2, "100.00", barrier)); // 口座1 → 口座2
            Thread b = new Thread(() -> results[1] = attempt(t2, 2, 1, "50.00", barrier));  // 口座2 → 口座1
            a.start();
            b.start();
            a.join();
            b.join();

            System.out.println("t1(口座1→2): " + results[0]);
            System.out.println("t2(口座2→1): " + results[1]);
            System.out.println("所要時間   : 約" + (System.nanoTime() - start) / 1_000_000 + " ms");
            System.out.println("最終残高   : " + Bank.summary(setup));
        }
    }

    static String attempt(Connection connection, long from, long to, String amount, CyclicBarrier barrier) {
        try {
            transfer(connection, from, to, new BigDecimal(amount), barrier);
            return "成功";
        } catch (SQLException e) {
            return "失敗 SQLState=" + e.getSQLState() + " " + e.getMessage().split("\n")[0];
        }
    }
}
