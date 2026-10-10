// 実行: mvn -q compile exec:java -Dexec.mainClass=PoolBasics
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PoolBasics {
    static int backendPid(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_backend_pid()");
             ResultSet results = statement.executeQuery()) {
            results.next();
            return results.getInt(1);
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. DriverManager:借りるたびに、新しい物理接続が作られる
        List<Integer> direct = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            try (Connection connection = Db.connect()) {
                direct.add(backendPid(connection));
            }
        }
        System.out.println("DriverManager の3回のサーバープロセス番号: " + direct);

        // 2. プール:close しても物理接続は閉じず、プールへ戻る
        try (HikariDataSource pool = Pool.create(2, 5000)) {
            List<Integer> pooled = new ArrayList<>();
            Connection last = null;
            for (int i = 0; i < 3; i++) {
                Connection connection = pool.getConnection();
                pooled.add(backendPid(connection));
                connection.close();
                last = connection;
            }
            System.out.println("プールの3回のサーバープロセス番号    : " + pooled);
            System.out.println("close した Connection の isClosed()  : " + last.isClosed());
            Thread.sleep(500); // プールが裏で接続を追加し終えるのを待つ
            System.out.println("プールの接続数: 合計 " + pool.getHikariPoolMXBean().getTotalConnections()
                    + " / 使用中 " + pool.getHikariPoolMXBean().getActiveConnections()
                    + " / 空き " + pool.getHikariPoolMXBean().getIdleConnections());
        }
    }
}
