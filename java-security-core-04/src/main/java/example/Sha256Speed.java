// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.Sha256Speed
package example;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

public class Sha256Speed {
    static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes()));
    }

    public static void main(String[] args) throws Exception {
        // 1. 1秒に何回、SHA-256を計算できるか(1つのスレッド)
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] input = "password123".getBytes();
        for (int i = 0; i < 2_000_000; i++) md.digest(input); // ウォームアップ
        long start = System.nanoTime();
        long count = 0;
        while (System.nanoTime() - start < 1_000_000_000L) {
            md.digest(input);
            count++;
        }
        System.out.println("SHA-256を、1つのスレッドで、1秒間に計算できた回数: " + String.format("%,d", count));

        // 2. ソルトがないと、同じパスワードは、同じハッシュになる
        System.out.println("## ソルトなし");
        System.out.println("  利用者A: " + sha256("password123"));
        System.out.println("  利用者B: " + sha256("password123"));

        // 3. 漏れたハッシュを、よく使われるパスワードの一覧から探す
        List<String> common = List.of("123456", "password", "password123", "qwerty", "letmein");
        String leaked = sha256("password123");
        System.out.println("## 漏れたハッシュを、一覧のハッシュと照合する(ソルトなし)");
        Map<String, String> table = new java.util.LinkedHashMap<>();
        for (String c : common) table.put(sha256(c), c);
        System.out.println("  照合の結果: " + table.getOrDefault(leaked, "見つからない"));

        // 4. ソルトがあれば、一覧のハッシュは使えない
        byte[] salt = new byte[16];
        new java.security.SecureRandom().nextBytes(salt);
        MessageDigest salted = MessageDigest.getInstance("SHA-256");
        salted.update(salt);
        String leakedSalted = HexFormat.of().formatHex(salted.digest("password123".getBytes()));
        System.out.println("## 漏れたハッシュ(ソルトあり)を、一覧のハッシュと照合する");
        System.out.println("  照合の結果: " + table.getOrDefault(leakedSalted, "見つからない"));
        System.out.println("  (ソルトを知っていれば、一覧の1語ごとに、ソルトつきで計算し直せる)");
    }
}
