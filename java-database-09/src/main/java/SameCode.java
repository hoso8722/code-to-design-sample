// 実行: mvn -q compile exec:java -Dexec.mainClass=SameCode
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SameCode {
    // 同じJDBCのコードを、3つのデータベースで動かす。表を作るSQLだけが、製品ごとに違う
    static void run(Engines.Engine engine) throws SQLException {
        try (Connection connection = engine.open(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS items");
            statement.execute("CREATE TABLE items (" + engine.idColumn() + ", name VARCHAR(50) NOT NULL, "
                    + "price " + engine.moneyType() + " NOT NULL, active " + engine.booleanType() + " NOT NULL)");

            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO items (name, price, active) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, "Apple");
                insert.setBigDecimal(2, new BigDecimal("1980.50"));
                insert.setBoolean(3, true);
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    keys.next();
                    System.out.println(engine.name() + ": 採番されたID=" + keys.getLong(1));
                }
            }

            try (PreparedStatement select = connection.prepareStatement("SELECT name, price, active FROM items WHERE name = ?");) {
                select.setString(1, "Apple");
                try (ResultSet results = select.executeQuery()) {
                    results.next();
                    System.out.println("  name=" + results.getString("name")
                            + " price=" + results.getBigDecimal("price")
                            + " active=" + results.getBoolean("active")
                            + " (priceのJDBC型=" + results.getMetaData().getColumnTypeName(2) + ")");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path file = Engines.newSqliteFile();
        try {
            for (Engines.Engine engine : Engines.all(file)) {
                run(engine);
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
