// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.SlowHashes
package example;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SlowHashes {
    static long millis(Runnable action, int times) {
        long start = System.nanoTime();
        for (int i = 0; i < times; i++) action.run();
        return (System.nanoTime() - start) / 1_000_000 / times;
    }

    static void pbkdf2(int iterations) {
        try {
            byte[] salt = new byte[16];
            PBEKeySpec spec = new PBEKeySpec("password123".toCharArray(), salt, iterations, 256);
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        // ウォームアップ
        pbkdf2(100_000);
        new BCryptPasswordEncoder(10).encode("warmup");

        System.out.println("## PBKDF2-HMAC-SHA256(JDKの標準)の、1回の計算時間");
        for (int iterations : new int[] {1_000, 100_000, 600_000}) {
            System.out.printf("  反復 %,9d 回: %4d ms%n", iterations, millis(() -> pbkdf2(iterations), 3));
        }

        System.out.println("## bcrypt(Spring Securityのcrypto)の、1回の計算時間");
        for (int strength : new int[] {4, 10, 12}) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(strength);
            System.out.printf("  strength %2d: %4d ms%n", strength, millis(() -> encoder.encode("password123"), 3));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String a = encoder.encode("password123");
        String b = encoder.encode("password123");
        System.out.println("## bcryptは、ソルトを、ハッシュの文字列に含める");
        System.out.println("  1回目: " + a);
        System.out.println("  2回目: " + b);
        System.out.println("  同じ文字列か: " + a.equals(b));
        System.out.println("  どちらも、元のパスワードと一致するか: " + encoder.matches("password123", a) + ", " + encoder.matches("password123", b));
        System.out.println("  誤ったパスワードと一致するか: " + encoder.matches("password124", a));
    }
}
