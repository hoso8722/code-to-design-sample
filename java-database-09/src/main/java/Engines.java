// Engines.java: 3つのデータベースへの接続と、製品ごとのSQLの違いをまとめた補助クラス
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

final class Engines {
    private Engines() {}

    // 製品ごとに、接続の作り方と、表を作るSQLの書き方が違う
    record Engine(String name, String idColumn, String moneyType, String booleanType, Connect connect) {
        Connection open() throws SQLException {
            return connect.open();
        }
    }

    @FunctionalInterface
    interface Connect {
        Connection open() throws SQLException;
    }

    static List<Engine> all(Path sqliteFile) {
        return List.of(
                new Engine("PostgreSQL 18", "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY", "NUMERIC(12, 2)", "BOOLEAN",
                        () -> DriverManager.getConnection(
                                Db.required("JDBC_URL"), Db.required("JDBC_USER"), Db.required("JDBC_PASSWORD"))),
                new Engine("MySQL 8.4", "id BIGINT AUTO_INCREMENT PRIMARY KEY", "DECIMAL(12, 2)", "BOOLEAN",
                        () -> DriverManager.getConnection(
                                Db.required("MYSQL_JDBC_URL"), Db.required("MYSQL_USER"), Db.required("MYSQL_PASSWORD"))),
                new Engine("SQLite", "id INTEGER PRIMARY KEY AUTOINCREMENT", "NUMERIC", "BOOLEAN",
                        () -> DriverManager.getConnection("jdbc:sqlite:" + sqliteFile)));
    }

    static Path newSqliteFile() throws java.io.IOException {
        Path file = Files.createTempFile("compare", ".db");
        Files.deleteIfExists(file);
        return file;
    }
}
