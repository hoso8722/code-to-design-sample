// 実行: mvn -q compile exec:java -Dexec.mainClass=RollbackDemo
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class RollbackDemo {
    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            Bank.reset(connection);
            System.out.println("開始前: " + Bank.summary(connection));

            // Rinへの加算は成功するが、Soraの残高を超える減算で制約違反になる
            try {
                Tx.inTransaction(connection, c -> {
                    Bank.update(c, 2, new BigDecimal("20000.00"));
                    Bank.update(c, 1, new BigDecimal("-20000.00"));
                    return null;
                });
            } catch (SQLException e) {
                System.out.println("制約違反  : SQLState=" + e.getSQLState());
            }
            System.out.println("失敗のあと: " + Bank.summary(connection));
            System.out.println("autoCommit: " + connection.getAutoCommit() + "(元の状態に戻っている)");
        }

        // 処理の途中で接続が使えなくなり、rollbackまで失敗する場合
        Connection broken = Db.connect();
        try {
            Tx.inTransaction(broken, c -> {
                Bank.update(c, 1, new BigDecimal("-100.00"));
                c.close(); // 接続が切れた状況を模擬する
                throw new SQLException("処理の途中で失敗した");
            });
        } catch (SQLException e) {
            System.out.println("投げられた例外: " + e.getMessage());
            System.out.println("rollbackの失敗: " + e.getSuppressed().length + "件を抑制して保持");
            System.out.println("  その内容    : " + e.getSuppressed()[0].getMessage());
        }
    }
}
