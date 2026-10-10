// 実行: mvn -q compile exec:java -Dexec.mainClass=OptimisticUpdate
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OptimisticUpdate {
    // バージョンを使わず、読んだ値から計算した値を書き込む(古い値を上書きする危険がある)
    static void blindUpdate(Connection connection, long id, int newStock) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE products SET stock = ? WHERE id = ?")) {
            statement.setInt(1, newStock);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection a = Db.connect(); Connection b = Db.connect()) {
            // 1. バージョンを使わない場合:2人が同じ在庫10を読み、それぞれ1つ売る
            Shop.reset(a, 1, 10);
            Shop.Row seenA = Shop.read(a, 1);
            Shop.Row seenB = Shop.read(b, 1);
            blindUpdate(a, 1, seenA.stock() - 1);
            blindUpdate(b, 1, seenB.stock() - 1);
            System.out.println("1. versionなし  : 2つ売ったのに在庫 " + Shop.read(a, 1).stock() + "(8になるはず)");

            // 2. バージョンつきの更新:同じ状況で、後からの更新は0行になる
            Shop.reset(a, 1, 10);
            seenA = Shop.read(a, 1);
            seenB = Shop.read(b, 1);
            System.out.println("2. AもBも読んだ : stock=" + seenA.stock() + " version=" + seenA.version());
            System.out.println("   Aの更新      : " + Shop.updateIfUnchanged(a, 1, seenA, seenA.stock() - 1) + "行");
            int rows = Shop.updateIfUnchanged(b, 1, seenB, seenB.stock() - 1);
            System.out.println("   Bの更新      : " + rows + "行 → 競合を検出");
            Shop.Row reloaded = Shop.read(b, 1);
            System.out.println("   Bが読み直す  : stock=" + reloaded.stock() + " version=" + reloaded.version());
            System.out.println("   Bの再更新    : " + Shop.updateIfUnchanged(b, 1, reloaded, reloaded.stock() - 1) + "行");
            Shop.Row last = Shop.read(a, 1);
            System.out.println("   最終         : stock=" + last.stock() + " version=" + last.version());
        }
    }
}
