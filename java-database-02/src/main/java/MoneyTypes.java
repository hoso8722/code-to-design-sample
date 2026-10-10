// 実行: mvn -q compile exec:java -Dexec.mainClass=MoneyTypes
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MoneyTypes {
    public static void main(String[] args) throws Exception {
        double d = 0.10 + 0.20;
        BigDecimal b = new BigDecimal("0.10").add(new BigDecimal("0.20"));
        System.out.println("double     : 0.10 + 0.20 = " + d);
        System.out.println("BigDecimal : 0.10 + 0.20 = " + b);

        try (Connection connection = Db.connect()) {
            Bank.reset(connection);
            // NUMERIC(12, 2) の列に、BigDecimalで加算して、BigDecimalで読む
            Bank.update(connection, 1, new BigDecimal("0.10"));
            Bank.update(connection, 1, new BigDecimal("0.20"));
            try (PreparedStatement statement = connection.prepareStatement("SELECT balance FROM accounts WHERE id = 1");
                 ResultSet results = statement.executeQuery()) {
                results.next();
                System.out.println("getBigDecimal : " + results.getBigDecimal(1));
                double asDouble = results.getDouble(1);
                System.out.println("getDouble     : " + asDouble);
                System.out.println("doubleの正確な値: " + new BigDecimal(asDouble));
            }
        }
    }
}
