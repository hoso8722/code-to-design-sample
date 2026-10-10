// 実行: mvn -q compile exec:java -Dexec.mainClass=MySqlCompare
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlCompare {
    static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                Db.required("MYSQL_JDBC_URL"), Db.required("MYSQL_USER"), Db.required("MYSQL_PASSWORD"));
    }

    static void reset(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS accounts");
            statement.execute("CREATE TABLE accounts (id BIGINT PRIMARY KEY, owner VARCHAR(50) NOT NULL, "
                    + "balance DECIMAL(12, 2) NOT NULL) ENGINE=InnoDB");
            statement.execute("INSERT INTO accounts VALUES (1, 'Sora', 10000.00), (2, 'Rin', 5000.00)");
        }
    }

    static BigDecimal balance(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT balance FROM accounts WHERE id = 1")) {
            results.next();
            return results.getBigDecimal(1);
        }
    }

    static void add(Connection connection, String amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE id = 1")) {
            statement.setBigDecimal(1, new BigDecimal(amount));
            statement.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = connect(); Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT version(), @@transaction_isolation")) {
            results.next();
            System.out.println("MySQL " + results.getString(1) + " / 既定の分離レベル: " + results.getString(2));
        }

        int[] levels = {Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ};
        for (int level : levels) {
            try (Connection t1 = connect(); Connection t2 = connect()) {
                reset(t2);
                t1.setTransactionIsolation(level);
                t1.setAutoCommit(false);

                BigDecimal first = balance(t1);
                add(t2, "1000.00");                  // t2が+1000円して確定する(autocommit)
                BigDecimal second = balance(t1);     // t1の2回目の読み取り

                String result;
                try {
                    add(t1, "100.00");               // t1が +100円 する
                    BigDecimal afterOwnUpdate = balance(t1);
                    t1.commit();
                    result = "更新に成功。自分の更新の直後に読むと " + afterOwnUpdate;
                } catch (SQLException e) {
                    t1.rollback();
                    result = "SQLState=" + e.getSQLState() + " " + e.getMessage();
                }
                System.out.printf("%-16s 残高 %s -> %s / t1の更新: %s / 最終残高=%s%n",
                        ReadTwice.name(level), first, second, result, balance(t2));
            }
        }
    }
}
