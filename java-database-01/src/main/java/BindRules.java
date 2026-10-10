// 実行: mvn -q compile exec:java -Dexec.mainClass=BindRules
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BindRules {
    static void attempt(String label, Connection connection, String sql, ThrowingBinder binder) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet results = statement.executeQuery()) {
                int rows = 0;
                while (results.next()) {
                    rows++;
                }
                System.out.println(label + " -> 成功(" + rows + "行)");
            }
        } catch (SQLException e) {
            System.out.println(label + " -> SQLState=" + e.getSQLState() + " " + firstLine(e.getMessage()));
        }
    }

    static String firstLine(String message) {
        int newline = message.indexOf('\n');
        return newline < 0 ? message : message.substring(0, newline);
    }

    interface ThrowingBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    // 並べ替えの列は、利用者の入力から選ばせるが、SQLに入れるのは許可した固定の文字列だけにする
    static String orderColumn(String choice) {
        return switch (choice) {
            case "email" -> "email";
            case "name" -> "display_name";
            default -> throw new IllegalArgumentException("並べ替えの項目が不正です: " + choice);
        };
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            Db.resetCustomers(connection);

            String sql = "SELECT id FROM customers WHERE email = ?";
            attempt("番号1に設定", connection, sql, s -> s.setString(1, "sora@example.com"));
            attempt("番号0に設定", connection, sql, s -> s.setString(0, "sora@example.com"));
            attempt("番号2に設定(? は1つ)", connection, sql, s -> s.setString(2, "sora@example.com"));
            attempt("値を設定しない", connection, sql, s -> { });
            attempt("表名を ? にする", connection, "SELECT id FROM ? WHERE email = 'sora@example.com'",
                    s -> s.setString(1, "customers"));

            for (String choice : new String[] {"name", "email", "id; DROP TABLE customers"}) {
                try {
                    String column = orderColumn(choice);
                    System.out.println("並べ替え " + choice + " -> ORDER BY " + column);
                } catch (IllegalArgumentException e) {
                    System.out.println("並べ替え " + choice + " -> " + e.getMessage());
                }
            }
        }
    }
}
