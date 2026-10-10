// Db.java: 接続情報を環境変数から読む補助クラス(#01と同じ)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
