// 実行: mvn -q compile exec:java -Dexec.mainClass=NaiveTx
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class NaiveTx {
    interface Step {
        void run() throws SQLException;
    }

    // 素直に書いた振替。SQLExceptionのときだけrollbackする
    static void transfer(Connection connection, BigDecimal amount, Step failureInTheMiddle) throws SQLException {
        connection.setAutoCommit(false);
        try {
            Bank.update(connection, 1, amount.negate());
            failureInTheMiddle.run();
            Bank.update(connection, 2, amount);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback(); // これが失敗すると、元の例外eは失われる
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. rollbackまで失敗する場合:元の例外が見えなくなる
        try (Connection setup = Db.connect()) {
            Bank.reset(setup);
        }
        Connection broken = Db.connect();
        try {
            transfer(broken, new BigDecimal("1000.00"), () -> {
                broken.close(); // 接続が切れた状況を模擬する
                throw new SQLException("集計の途中で失敗した");
            });
        } catch (Exception e) {
            System.out.println("1. 本来の失敗      : 集計の途中で失敗した");
            System.out.println("   投げられた例外  : " + e.getClass().getSimpleName() + " / " + e.getMessage());
        }

        // 2. 実行時例外のとき:rollbackされず、autoCommitも戻らない
        try (Connection connection = Db.connect(); Connection observer = Db.connect()) {
            Bank.reset(connection);
            try {
                transfer(connection, new BigDecimal("1000.00"), () -> {
                    throw new IllegalStateException("途中の処理で失敗した");
                });
            } catch (IllegalStateException e) {
                System.out.println("2. 投げられた例外: " + e.getMessage());
            }
            System.out.println("   autoCommit             : " + connection.getAutoCommit());
            System.out.println("   同じ接続で読む         : " + Bank.summary(connection));
            System.out.println("   別の接続で読む         : " + Bank.summary(observer));

            // この接続を使う、別の処理が、あとでcommitを呼んだとする
            connection.commit();
            System.out.println("   あとからcommitした後   : " + Bank.summary(observer));
        }
    }
}
