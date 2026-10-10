// 実行: mvn -q compile exec:java -Dexec.mainClass=RetrySerializable
import java.sql.Connection;
import java.sql.SQLException;

public class RetrySerializable {
    // SERIALIZABLEで失敗(SQLState 40001)したら、トランザクション全体をやり直す
    static String goOffDuty(Connection connection, String name, int maxAttempts) throws SQLException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                int onDuty = WriteSkew.onDutyCount(connection);
                if (onDuty < 2) {
                    connection.rollback();
                    return name + ": やり直し" + attempt + "回目 当直が" + onDuty + "人なので抜けない";
                }
                WriteSkew.leave(connection, name);
                connection.commit();
                return name + ": やり直し" + attempt + "回目 当直が" + onDuty + "人なので抜けた";
            } catch (SQLException e) {
                connection.rollback();
                if (!"40001".equals(e.getSQLState()) || attempt == maxAttempts) {
                    throw e;
                }
                System.out.println(name + ": やり直し" + attempt + "回目 SQLState=40001 で失敗。最初からやり直す");
            }
        }
        throw new IllegalStateException("到達しない");
    }

    public static void main(String[] args) throws Exception {
        try (Connection t1 = Db.connect(); Connection t2 = Db.connect(); Connection check = Db.connect()) {
            WriteSkew.reset(check);
            for (Connection c : new Connection[] {t1, t2}) {
                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                c.setAutoCommit(false);
            }
            // 2つが重なるように、お互いの読み取りを済ませてから更新する
            int seen1 = WriteSkew.onDutyCount(t1);
            int seen2 = WriteSkew.onDutyCount(t2);
            WriteSkew.leave(t1, "Alice");
            t1.commit();
            System.out.println("Alice: 試行1 当直が" + seen1 + "人なので抜けた");
            try {
                WriteSkew.leave(t2, "Bob");
                t2.commit();
                System.out.println("Bob: 試行1 当直が" + seen2 + "人なので抜けた");
            } catch (SQLException e) {
                t2.rollback();
                System.out.println("Bob: 試行1 SQLState=" + e.getSQLState() + " で失敗。最初からやり直す");
                System.out.println(goOffDuty(t2, "Bob", 3));
            }
            System.out.println("最終的な当直: " + WriteSkew.onDutyCount(check) + "人");
        }
    }
}
