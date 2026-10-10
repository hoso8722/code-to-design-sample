// 実行: mvn -q compile exec:java -Dexec.mainClass=UpdateConflict
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class UpdateConflict {
    public static void main(String[] args) throws Exception {
        int[] levels = {
            Connection.TRANSACTION_READ_COMMITTED,
            Connection.TRANSACTION_REPEATABLE_READ,
            Connection.TRANSACTION_SERIALIZABLE,
        };
        for (int level : levels) {
            try (Connection t1 = Db.connect(); Connection t2 = Db.connect()) {
                Bank.reset(t2);
                t1.setTransactionIsolation(level);
                t1.setAutoCommit(false);

                BigDecimal seen = Bank.balance(t1, 1);                // t1が10000円を読む
                Bank.update(t2, 1, new BigDecimal("1000.00"));        // t2が+1000円して確定する
                String result;
                try {
                    Bank.update(t1, 1, new BigDecimal("100.00"));     // t1が +100円 しようとする
                    t1.commit();
                    result = "更新に成功";
                } catch (SQLException e) {
                    t1.rollback();
                    result = "SQLState=" + e.getSQLState() + " " + e.getMessage().split("\n")[0];
                }
                System.out.printf("%-16s t1が読んだ値=%s / t1の更新: %s / 最終残高=%s%n",
                        ReadTwice.name(level), seen, result, Bank.balance(t2, 1));
            }
        }
    }
}
