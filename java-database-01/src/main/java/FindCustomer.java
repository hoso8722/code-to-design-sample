// 実行: mvn -q compile exec:java -Dexec.mainClass=FindCustomer -Dexec.args=sora@example.com
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FindCustomer {
    private static final String SQL = "SELECT id, display_name FROM customers WHERE email = ?";

    public static void main(String[] args) throws SQLException {
        if (args.length != 1) {
            throw new IllegalArgumentException("検索するメールアドレスを1つ指定してください");
        }
        // Connection、PreparedStatement、ResultSetは、try-with-resourcesで内側から順に閉じる
        try (Connection connection = Db.connect();
             PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, args[0]);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    System.out.println("該当する顧客はいません");
                    return;
                }
                System.out.printf("%d: %s%n", results.getLong("id"), results.getString("display_name"));
            }
        }
    }
}
