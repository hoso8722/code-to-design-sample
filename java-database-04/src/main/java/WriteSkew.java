// 実行: mvn -q compile exec:java -Dexec.mainClass=WriteSkew
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WriteSkew {
    // 「当直の医師は、常に1人以上いる」という規則。2人とも当直で、お互いが「自分は抜けてよい」と判断する
    static void reset(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS doctors");
            statement.execute("CREATE TABLE doctors (name TEXT PRIMARY KEY, on_duty BOOLEAN NOT NULL)");
            statement.execute("INSERT INTO doctors VALUES ('Alice', true), ('Bob', true)");
        }
    }

    static int onDutyCount(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT count(*) FROM doctors WHERE on_duty")) {
            results.next();
            return results.getInt(1);
        }
    }

    // 自分の当直を外す
    static void leave(Connection connection, String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE doctors SET on_duty = false WHERE name = '" + name + "'");
        }
    }

    static String commitResult(Connection connection) {
        try {
            connection.commit();
            return "commit成功";
        } catch (SQLException e) {
            return "commit失敗(SQLState=" + e.getSQLState() + ")";
        }
    }

    public static void main(String[] args) throws Exception {
        int[] levels = {
            Connection.TRANSACTION_READ_COMMITTED,
            Connection.TRANSACTION_REPEATABLE_READ,
            Connection.TRANSACTION_SERIALIZABLE,
        };
        for (int level : levels) {
            try (Connection t1 = Db.connect(); Connection t2 = Db.connect(); Connection check = Db.connect()) {
                reset(check);
                for (Connection c : new Connection[] {t1, t2}) {
                    c.setTransactionIsolation(level);
                    c.setAutoCommit(false);
                }
                // 2つのトランザクションが、お互いの更新を見ないまま、「2人いるから自分は抜けてよい」と判断する
                int seen1 = onDutyCount(t1);
                int seen2 = onDutyCount(t2);
                leave(t1, "Alice");
                String result1 = commitResult(t1);
                String result2;
                try {
                    leave(t2, "Bob");
                    result2 = commitResult(t2);
                } catch (SQLException e) {
                    t2.rollback();
                    result2 = "更新で失敗(SQLState=" + e.getSQLState() + ")";
                }
                System.out.printf("%-16s t1は%d人と見て%s / t2は%d人と見て%s / 最終的な当直 %d人%n",
                        ReadTwice.name(level), seen1, result1, seen2, result2, onDutyCount(check));
            }
        }
    }
}
