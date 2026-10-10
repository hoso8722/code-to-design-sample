// 実行: mvn -q compile exec:java -Dexec.mainClass=ConnectionLimit
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ConnectionLimit {
    public static void main(String[] args) throws Exception {
        try (Connection connection = Db.connect(); Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT current_setting('max_connections'), current_setting('superuser_reserved_connections')")) {
            results.next();
            System.out.println("max_connections=" + results.getString(1)
                    + " superuser_reserved_connections=" + results.getString(2));
        }

        // プールを使わず、接続を開けられるだけ開けていく
        List<Connection> opened = new ArrayList<>();
        try {
            for (int i = 1; i <= 150; i++) {
                opened.add(Db.connect());
            }
            System.out.println("150本まで開けた");
        } catch (SQLException e) {
            System.out.println("開けた接続: " + opened.size() + "本");
            System.out.println("失敗: SQLState=" + e.getSQLState() + " " + e.getMessage());
        } finally {
            for (Connection connection : opened) {
                connection.close();
            }
        }
    }
}
