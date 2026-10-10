// 実行: mvn -q compile exec:java -Dexec.mainClass=AbortedState
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AbortedState {
    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            Bank.reset(connection);
            connection.setAutoCommit(false);

            // 残高(10000円)を超える額を引き出すと、CHECK制約に違反して失敗する
            try {
                Bank.update(connection, 1, new BigDecimal("-20000.00"));
            } catch (SQLException e) {
                System.out.println("1つ目の失敗 : SQLState=" + e.getSQLState());
            }

            // 失敗のあとに、別の正しいSQLを実行しても、トランザクションは使えない
            try {
                Bank.update(connection, 2, new BigDecimal("100.00"));
            } catch (SQLException e) {
                System.out.println("そのあとのSQL: SQLState=" + e.getSQLState());
            }

            connection.rollback();
            Bank.update(connection, 2, new BigDecimal("100.00"));
            connection.commit();
            System.out.println("rollbackのあと: " + Bank.summary(connection));
        }
    }
}
