// Db.java: 接続情報を環境変数から読む補助クラス(各プログラムから使う)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// 接続情報を環境変数から読む。パスワードなどはソースコードに書かない
final class Db {
    private Db() {}

    static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                required("JDBC_URL"), required("JDBC_USER"), required("JDBC_PASSWORD"));
    }

    static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("環境変数が設定されていません: " + name);
        }
        return value;
    }

    // 実験用のcustomers表を作り直す
    static void resetCustomers(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS customers");
            statement.execute("""
                    CREATE TABLE customers (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        email TEXT NOT NULL UNIQUE,
                        display_name TEXT NOT NULL
                    )""");
            statement.execute("""
                    INSERT INTO customers (email, display_name) VALUES
                        ('sora@example.com', '空'),
                        ('rin@example.com', '凛'),
                        ('admin@example.com', '管理者')""");
        }
    }
}
