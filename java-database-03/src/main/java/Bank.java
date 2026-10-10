// Bank.java: 口座の表を作り、残高を読む補助クラス
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

final class Bank {
    private Bank() {}

    // 実験用のaccounts表を作り直す(Sora 10000円、Rin 5000円)
    static void reset(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS accounts");
            statement.execute("""
                    CREATE TABLE accounts (
                        id BIGINT PRIMARY KEY,
                        owner TEXT NOT NULL,
                        balance NUMERIC(12, 2) NOT NULL CHECK (balance >= 0)
                    )""");
            statement.execute("INSERT INTO accounts VALUES (1, 'Sora', 10000.00), (2, 'Rin', 5000.00)");
        }
    }

    static BigDecimal balance(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getBigDecimal(1);
            }
        }
    }

    static String summary(Connection connection) throws SQLException {
        BigDecimal sora = balance(connection, 1);
        BigDecimal rin = balance(connection, 2);
        return "Sora=" + sora + " Rin=" + rin + " 合計=" + sora.add(rin);
    }

    static void update(Connection connection, long id, BigDecimal delta) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = ?")) {
            statement.setBigDecimal(1, delta);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}
