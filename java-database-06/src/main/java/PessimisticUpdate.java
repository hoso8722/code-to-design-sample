// 実行: mvn -q compile exec:java -Dexec.mainClass=PessimisticUpdate
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PessimisticUpdate {
    static final long ORIGIN = System.nanoTime();

    static void log(String message) {
        System.out.printf("%5d ms  %s%n", (System.nanoTime() - ORIGIN) / 1_000_000, message);
    }

    // 行をロックして読み、在庫を1つ減らして commit する
    static void sell(Connection connection, String who, long workMillis) throws SQLException, InterruptedException {
        connection.setAutoCommit(false);
        log(who + ": SELECT FOR UPDATE を発行");
        int stock;
        try (PreparedStatement statement = connection.prepareStatement("SELECT stock FROM products WHERE id = 1 FOR UPDATE");
             ResultSet results = statement.executeQuery()) {
            results.next();
            stock = results.getInt(1);
        }
        log(who + ": ロックを取って在庫 " + stock + " を読んだ");
        Thread.sleep(workMillis); // 業務の処理に時間がかかる
        try (PreparedStatement statement = connection.prepareStatement("UPDATE products SET stock = ? WHERE id = 1")) {
            statement.setInt(1, stock - 1);
            statement.executeUpdate();
        }
        connection.commit();
        log(who + ": 在庫を " + (stock - 1) + " にして commit");
    }

    public static void main(String[] args) throws Exception {
        try (Connection a = Db.connect(); Connection b = Db.connect()) {
            Shop.reset(a, 1, 10);
            Thread other = new Thread(() -> {
                try {
                    Thread.sleep(100);
                    sell(b, "B", 0);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            other.start();
            sell(a, "A", 600);
            other.join();
            log("最終: 在庫 " + Shop.read(a, 1).stock());
        }
    }
}
