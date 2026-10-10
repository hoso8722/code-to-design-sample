// 実行: mvn -q compile exec:java -Dexec.mainClass=Behaviors
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Behaviors {
    interface Check {
        String run(Engines.Engine engine) throws Exception;
    }

    static void setUp(Engines.Engine engine, Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS items");
            statement.execute("CREATE TABLE items (" + engine.idColumn() + ", name VARCHAR(50) NOT NULL, "
                    + "price " + engine.moneyType() + " NOT NULL)");
            statement.execute("INSERT INTO items (name, price) VALUES ('Apple', 100), ('Banana', 200)");
        }
    }

    // 1. 文字列の比較:保存した 'Apple' を 'apple' で探す
    static String caseSensitivity(Engines.Engine engine) throws Exception {
        try (Connection connection = engine.open()) {
            setUp(engine, connection);
            try (PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM items WHERE name = ?")) {
                statement.setString(1, "apple");
                try (ResultSet results = statement.executeQuery()) {
                    results.next();
                    return results.getInt(1) + "件";
                }
            }
        }
    }

    // 2. DDLをトランザクションの中で実行して、rollbackすると、表は残るか
    static String ddlRollback(Engines.Engine engine) throws Exception {
        try (Connection connection = engine.open()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS ddl_probe");
            }
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE ddl_probe (id INT)");
            }
            connection.rollback();
            connection.setAutoCommit(true);
            try (ResultSet tables = connection.getMetaData().getTables(null, null, "ddl_probe", null)) {
                boolean exists = tables.next();
                try (Statement statement = connection.createStatement()) {
                    statement.execute("DROP TABLE IF EXISTS ddl_probe");
                }
                return exists ? "表が残った" : "表は消えた";
            }
        }
    }

    // 3. 2つの接続が、別々の行を同時に更新する。1つ目は commit していない
    //    byPrimaryKey が true なら主キーで行を指定し、false なら索引のない列(name)で指定する
    static String concurrentWrite(Engines.Engine engine, boolean byPrimaryKey) throws Exception {
        try (Connection first = engine.open(); Connection second = engine.open()) {
            setUp(engine, first);
            first.setAutoCommit(false);
            String firstWhere = byPrimaryKey ? "id = 1" : "name = 'Apple'";
            String secondWhere = byPrimaryKey ? "id = 2" : "name = 'Banana'";
            try (Statement statement = first.createStatement()) {
                statement.executeUpdate("UPDATE items SET price = price + 1 WHERE " + firstWhere); // commitしない
            }
            // 待つ上限を短くして、実験が長引かないようにする
            try (Statement statement = second.createStatement()) {
                switch (engine.name()) {
                    case "SQLite" -> statement.execute("PRAGMA busy_timeout = 500");
                    case "MySQL 8.4" -> statement.execute("SET SESSION innodb_lock_wait_timeout = 2");
                    default -> statement.execute("SET lock_timeout = '2s'");
                }
            }
            long start = System.nanoTime();
            try (Statement statement = second.createStatement()) {
                statement.executeUpdate("UPDATE items SET price = price + 1 WHERE " + secondWhere);
                return "成功(" + (System.nanoTime() - start) / 1_000_000 + "ms)";
            } catch (SQLException e) {
                return "失敗(約" + (System.nanoTime() - start) / 1_000_000 + "ms) " + e.getMessage().split("\n")[0];
            } finally {
                first.rollback();
            }
        }
    }

    // 4. INSERT ... RETURNING が使えるか
    static String returning(Engines.Engine engine) throws Exception {
        try (Connection connection = engine.open()) {
            setUp(engine, connection);
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("INSERT INTO items (name, price) VALUES ('Cherry', 300) RETURNING id")) {
                results.next();
                return "使える(id=" + results.getLong(1) + ")";
            } catch (SQLException e) {
                return "使えない(SQLState=" + e.getSQLState() + ")";
            }
        }
    }

    // 5. 接続したときの、既定の分離レベル
    static String isolation(Engines.Engine engine) throws Exception {
        try (Connection connection = engine.open()) {
            return switch (connection.getTransactionIsolation()) {
                case Connection.TRANSACTION_READ_COMMITTED -> "READ COMMITTED";
                case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE READ";
                case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
                default -> "その他";
            };
        }
    }

    public static void main(String[] args) throws Exception {
        Path file = Engines.newSqliteFile();
        try {
            List<Engines.Engine> engines = Engines.all(file);
            String[][] checks = {{"1. 'Apple' を 'apple' で探す"}, {"2. DDLを rollback"},
                    {"3. 別の行を、主キーで指定して同時に更新"}, {"4. 別の行を、索引のない列で指定して同時に更新"},
                    {"5. INSERT ... RETURNING"}, {"6. 既定の分離レベル"}};
            Check[] runners = {Behaviors::caseSensitivity, Behaviors::ddlRollback,
                    engine -> concurrentWrite(engine, true), engine -> concurrentWrite(engine, false),
                    Behaviors::returning, Behaviors::isolation};
            for (int i = 0; i < runners.length; i++) {
                System.out.println(checks[i][0]);
                for (Engines.Engine engine : engines) {
                    System.out.println("  " + engine.name() + ": " + runners[i].run(engine));
                }
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
