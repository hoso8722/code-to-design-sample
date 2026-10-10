// 実行: mvn -q compile exec:java -Dexec.mainClass=DeferredCommit
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DeferredCommit {
    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS deferred_demo");
                statement.execute("CREATE TABLE deferred_demo (id INT, CONSTRAINT deferred_unique UNIQUE (id) DEFERRABLE INITIALLY DEFERRED)");
            }
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO deferred_demo VALUES (1)");
                statement.execute("INSERT INTO deferred_demo VALUES (1)"); // この時点では失敗しない
                System.out.println("2件目のINSERT: 成功(検査はcommitまで遅れる)");
            }
            try {
                connection.commit();
            } catch (SQLException e) {
                System.out.println("commit: 失敗 SQLState=" + e.getSQLState());
            }
            connection.rollback();
            System.out.println("そのあとのrollback: 例外なし");
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT count(*) FROM deferred_demo")) {
                results.next();
                System.out.println("deferred_demo の行数: " + results.getInt(1));
                statement.execute("DROP TABLE deferred_demo");
            }
        }
    }
}
