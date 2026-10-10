// Tx.java: トランザクションの開始、commit、rollbackをまとめた補助クラス
import java.sql.Connection;
import java.sql.SQLException;

final class Tx {
    private Tx() {}

    @FunctionalInterface
    interface Work<T> {
        T run(Connection connection) throws SQLException;
    }

    static <T> T inTransaction(Connection connection, Work<T> work) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            T result = work.run(connection);
            connection.commit();
            return result;
        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                // rollback自体が失敗しても、元の例外を隠さない
                e.addSuppressed(rollbackFailure);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException ignored) {
                // 接続がすでに使えない場合は、戻せなくてよい
            }
        }
    }
}
