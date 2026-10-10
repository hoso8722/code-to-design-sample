// 実行: mvn -q compile exec:java -Dexec.mainClass=CloseBehavior
import java.math.BigDecimal;
import java.sql.Connection;

public class CloseBehavior {
    // close時に例外を投げるリソース
    static class FaultyResource implements AutoCloseable {
        @Override
        public void close() throws Exception {
            throw new IllegalStateException("closeで失敗した");
        }
    }

    public static void main(String[] args) throws Exception {
        // 1. トランザクションを終えずに close するとどうなるか(PostgreSQLでの結果)
        try (Connection observer = Db.connect()) {
            Bank.reset(observer);
            Connection connection = Db.connect();
            connection.setAutoCommit(false);
            Bank.update(connection, 1, new BigDecimal("-500.00"));
            System.out.println("close前(自分)  : " + Bank.summary(connection));
            connection.close(); // commitもrollbackもしない
            System.out.println("close後(別接続): " + Bank.summary(observer));
        }

        // 2. try-with-resourcesで、本体の例外とcloseの例外が両方出る場合
        try (FaultyResource resource = new FaultyResource()) {
            throw new IllegalArgumentException("本体で失敗した");
        } catch (Exception e) {
            System.out.println("主たる例外  : " + e.getMessage());
            System.out.println("抑制された例外: " + e.getSuppressed()[0].getMessage());
        }
    }
}
