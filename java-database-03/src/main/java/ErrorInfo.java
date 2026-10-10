// 実行: mvn -q compile exec:java -Dexec.mainClass=ErrorInfo
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;

public class ErrorInfo {
    // ログに出してよい情報だけを取り出す。メッセージ本文にはデータの値が入ることがあるので含めない
    static String forLog(SQLException e) {
        String constraint = (e instanceof PSQLException pe && pe.getServerErrorMessage() != null)
                ? pe.getServerErrorMessage().getConstraint() : null;
        return "SQLState=" + e.getSQLState() + " vendorCode=" + e.getErrorCode() + " constraint=" + constraint;
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            Bank.reset(connection);

            try {
                Bank.update(connection, 1, new BigDecimal("-20000.00")); // CHECK制約に違反する
            } catch (SQLException e) {
                System.out.println("1. CHECK制約違反");
                System.out.println("  getMessage : " + e.getMessage().replace("\n", " / "));
                System.out.println("  ログ用     : " + forLog(e));
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO accounts (id, owner, balance) VALUES (?, ?, ?)")) {
                statement.setLong(1, 1); // すでにあるID
                statement.setString(2, "Dup");
                statement.setBigDecimal(3, new BigDecimal("1.00"));
                statement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("2. 主キーの重複");
                System.out.println("  getMessage : " + e.getMessage().replace("\n", " / "));
                System.out.println("  ログ用     : " + forLog(e));
            }
        }
    }
}
