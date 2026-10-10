// 実行: mvn -q compile exec:java -Dexec.mainClass=TransactionalTransfer
import java.math.BigDecimal;
import java.sql.Connection;

public class TransactionalTransfer {
    // 2つの更新を1つのトランザクションにまとめる
    static void transfer(Connection connection, long from, long to, BigDecimal amount, boolean failInTheMiddle)
            throws Exception {
        connection.setAutoCommit(false);
        try {
            Bank.update(connection, from, amount.negate());
            if (failInTheMiddle) {
                throw new IllegalStateException("減算のあとで失敗した(模擬)");
            }
            Bank.update(connection, to, amount);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection main = Db.connect(); Connection observer = Db.connect()) {
            Bank.reset(main);
            System.out.println("開始前          : " + Bank.summary(main));

            // 失敗する振替:rollbackで、減算も取り消される
            try {
                transfer(main, 1, 2, new BigDecimal("3000.00"), true);
            } catch (Exception e) {
                System.out.println("例外            : " + e.getMessage());
            }
            System.out.println("失敗のあと      : " + Bank.summary(main));

            // 成功する振替:commitするまで、別の接続からは変更が見えない
            main.setAutoCommit(false);
            Bank.update(main, 1, new BigDecimal("-3000.00"));
            Bank.update(main, 2, new BigDecimal("3000.00"));
            System.out.println("commit前(自分)  : " + Bank.summary(main));
            System.out.println("commit前(別接続): " + Bank.summary(observer));
            main.commit();
            main.setAutoCommit(true);
            System.out.println("commit後(別接続): " + Bank.summary(observer));
        }
    }
}
