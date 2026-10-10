// 実行: mvn -q compile exec:java -Dexec.mainClass=InjectionDemo
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InjectionDemo {
    // 文字列連結でSQLを組み立てる書き方
    static List<String> findByConcat(Connection connection, String email) throws SQLException {
        String sql = "SELECT display_name FROM customers WHERE email = '" + email + "'";
        List<String> names = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {
            while (results.next()) {
                names.add(results.getString("display_name"));
            }
        }
        return names;
    }

    // PreparedStatementで、値を別に渡す書き方
    static List<String> findByPrepared(Connection connection, String email) throws SQLException {
        List<String> names = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT display_name FROM customers WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    names.add(results.getString("display_name"));
                }
            }
        }
        return names;
    }

    static String describe(Connection connection, String email, boolean prepared) {
        try {
            List<String> names = prepared ? findByPrepared(connection, email) : findByConcat(connection, email);
            return names.toString();
        } catch (SQLException e) {
            return "SQLException(SQLState=" + e.getSQLState() + ")";
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect()) {
            Db.resetCustomers(connection);

            String[] inputs = {
                "sora@example.com",
                "' OR '1'='1' -- ",
                "o'brien@example.com",
                "x'; UPDATE customers SET display_name = 'hacked' WHERE 'a' = 'a",
            };
            for (String input : inputs) {
                System.out.println("入力: " + input);
                System.out.println("  文字列連結     : " + describe(connection, input, false));
                System.out.println("  PreparedStatement: " + describe(connection, input, true));
            }

            System.out.println();
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT display_name FROM customers ORDER BY id")) {
                List<String> names = new ArrayList<>();
                while (results.next()) {
                    names.add(results.getString(1));
                }
                System.out.println("実験のあとの表示名: " + names);
            }
        }
    }
}
