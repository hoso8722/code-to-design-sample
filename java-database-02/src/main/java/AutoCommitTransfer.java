// 実行: mvn -q compile exec:java -Dexec.mainClass=AutoCommitTransfer
import java.math.BigDecimal;
import java.sql.Connection;

public class AutoCommitTransfer {
    // 振替:from の残高を減らし、to の残高を増やす。途中で失敗する場合を模擬できる
    static void transfer(Connection connection, long from, long to, BigDecimal amount, boolean failInTheMiddle)
            throws Exception {
        Bank.update(connection, from, amount.negate());
        if (failInTheMiddle) {
            throw new IllegalStateException("減算のあとで失敗した(模擬)");
        }
        Bank.update(connection, to, amount);
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            System.out.println("新しい接続の autoCommit: " + connection.getAutoCommit());
            Bank.reset(connection);
            System.out.println("開始前    : " + Bank.summary(connection));

            try {
                transfer(connection, 1, 2, new BigDecimal("3000.00"), true);
            } catch (Exception e) {
                System.out.println("例外      : " + e.getMessage());
            }
            System.out.println("失敗のあと: " + Bank.summary(connection));
        }
    }
}
