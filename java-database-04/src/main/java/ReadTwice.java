// 実行: mvn -q compile exec:java -Dexec.mainClass=ReadTwice
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReadTwice {
    static String name(int level) {
        return switch (level) {
            case Connection.TRANSACTION_READ_COMMITTED -> "READ COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "?";
        };
    }

    static int count(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery("SELECT count(*) FROM accounts")) {
            results.next();
            return results.getInt(1);
        }
    }

    // t2が更新したが、まだ確定していないときに、t1が読むとどう見えるか
    static BigDecimal readWhileUncommitted(int level) throws Exception {
        try (Connection t1 = Db.connect(); Connection t2 = Db.connect()) {
            Bank.reset(t2);
            t2.setAutoCommit(false);
            Bank.update(t2, 1, new BigDecimal("1000.00")); // 確定しない
            t1.setTransactionIsolation(level);
            t1.setAutoCommit(false);
            BigDecimal seen = Bank.balance(t1, 1);
            t1.commit();
            t2.rollback();
            return seen;
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect(); Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT version(), current_setting('default_transaction_isolation')")) {
            results.next();
            System.out.println(results.getString(1).split(",")[0] + " / 既定の分離レベル: " + results.getString(2));
        }

        int[] levels = {
            Connection.TRANSACTION_READ_COMMITTED,
            Connection.TRANSACTION_REPEATABLE_READ,
            Connection.TRANSACTION_SERIALIZABLE,
        };
        for (int level : levels) {
            try (Connection t1 = Db.connect(); Connection t2 = Db.connect()) {
                Bank.reset(t2);
                t1.setTransactionIsolation(level);
                t1.setAutoCommit(false);

                BigDecimal balance1 = Bank.balance(t1, 1);   // t1の1回目の読み取り
                int count1 = count(t1);

                Bank.update(t2, 1, new BigDecimal("1000.00")); // t2が更新して確定する(autocommit)
                try (PreparedStatement insert = t2.prepareStatement(
                        "INSERT INTO accounts (id, owner, balance) VALUES (3, 'Hal', 1.00)")) {
                    insert.executeUpdate();
                }

                BigDecimal balance2 = Bank.balance(t1, 1);   // t1の2回目の読み取り
                int count2 = count(t1);
                t1.commit();

                System.out.printf("%-16s 残高 %s -> %s / 口座数 %d -> %d / 未確定の更新を読むと %s%n",
                        name(level), balance1, balance2, count1, count2, readWhileUncommitted(level));
            }
        }
    }
}
