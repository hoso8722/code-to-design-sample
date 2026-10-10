// Shop.java: 商品の表を作り、在庫を読み書きする補助クラス
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class Shop {
    private Shop() {}

    // 読み取った時点の在庫とバージョン
    record Row(int stock, int version) {}

    // products表を作り直す。各商品の在庫は initialStock、バージョンは0
    static void reset(Connection connection, int rows, int initialStock) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS products");
            statement.execute("""
                    CREATE TABLE products (
                        id BIGINT PRIMARY KEY,
                        stock INT NOT NULL CHECK (stock >= 0),
                        version INT NOT NULL
                    )""");
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO products VALUES (?, ?, 0)")) {
            for (int id = 1; id <= rows; id++) {
                statement.setLong(1, id);
                statement.setInt(2, initialStock);
                statement.executeUpdate();
            }
        }
    }

    static Row read(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock, version FROM products WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return new Row(results.getInt(1), results.getInt(2));
            }
        }
    }

    // 楽観ロック:読んだときのバージョンのままなら更新し、バージョンを1つ進める。更新できた行数を返す
    static int updateIfUnchanged(Connection connection, long id, Row seen, int newStock) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE products SET stock = ?, version = version + 1 WHERE id = ? AND version = ?")) {
            statement.setInt(1, newStock);
            statement.setLong(2, id);
            statement.setInt(3, seen.version());
            return statement.executeUpdate();
        }
    }
}
