// 実行: mvn -q compile exec:java -Dexec.mainClass=OrderedLocks
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CyclicBarrier;

public class OrderedLocks {
    // 方法1:ロックを取る順番を、口座IDの小さい順にそろえる
    static void transferOrdered(Connection connection, long from, long to, BigDecimal amount) throws SQLException {
        connection.setAutoCommit(false);
        try {
            ForUpdateWait.lockRow(connection, Math.min(from, to));
            ForUpdateWait.lockRow(connection, Math.max(from, to));
            Bank.update(connection, from, amount.negate());
            Bank.update(connection, to, amount);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        }
    }

    // 方法2:順番はそろえず、デッドロック(40P01)などで失敗したら、トランザクション全体をやり直す
    static int transferWithRetry(Connection connection, long from, long to, BigDecimal amount, CyclicBarrier barrier,
                                 int maxAttempts) throws SQLException {
        for (int attempt = 1; ; attempt++) {
            try {
                // 1回目だけ待ち合わせて、デッドロックを確実に起こす。やり直しでは待ち合わせない
                DeadlockDemo.transfer(connection, from, to, amount, attempt == 1 ? barrier : null);
                return attempt;
            } catch (SQLException e) {
                boolean retryable = "40P01".equals(e.getSQLState()) || "40001".equals(e.getSQLState());
                if (!retryable || attempt == maxAttempts) {
                    throw e;
                }
                System.out.println("  SQLState=" + e.getSQLState() + " で失敗。やり直し" + attempt + "回目");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection setup = Db.connect(); Connection t1 = Db.connect(); Connection t2 = Db.connect()) {
            // 方法1
            Bank.reset(setup);
            long start = System.nanoTime();
            Thread a = new Thread(() -> run(() -> transferOrdered(t1, 1, 2, new BigDecimal("100.00"))));
            Thread b = new Thread(() -> run(() -> transferOrdered(t2, 2, 1, new BigDecimal("50.00"))));
            a.start();
            b.start();
            a.join();
            b.join();
            System.out.println("方法1(ロックの順番をそろえる): 約" + (System.nanoTime() - start) / 1_000_000
                    + " ms / 最終残高 " + Bank.summary(setup));

            // 方法2
            Bank.reset(setup);
            CyclicBarrier barrier = new CyclicBarrier(2);
            int[] attempts = new int[2];
            start = System.nanoTime();
            System.out.println("方法2(失敗したらやり直す):");
            Thread c = new Thread(() -> attempts[0] = runInt(() -> transferWithRetry(t1, 1, 2, new BigDecimal("100.00"), barrier, 3)));
            Thread d = new Thread(() -> attempts[1] = runInt(() -> transferWithRetry(t2, 2, 1, new BigDecimal("50.00"), barrier, 3)));
            c.start();
            d.start();
            c.join();
            d.join();
            System.out.println("  試行回数 t1=" + attempts[0] + " t2=" + attempts[1] + " / 約"
                    + (System.nanoTime() - start) / 1_000_000 + " ms / 最終残高 " + Bank.summary(setup));
        }
    }

    interface Action {
        void run() throws SQLException;
    }

    interface IntAction {
        int run() throws SQLException;
    }

    static void run(Action action) {
        try {
            action.run();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static int runInt(IntAction action) {
        try {
            return action.run();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
