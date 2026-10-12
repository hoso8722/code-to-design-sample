// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.Migration
package example;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Migration {
    public static void main(String[] args) {
        // 既定の組み立て:新しいハッシュは、先頭に方式の名前({bcrypt})を付けて保存する
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String newHash = encoder.encode("password123");
        System.out.println("新しく作るハッシュの先頭: " + newHash.substring(0, 18) + " ...");

        // 昔のコストの低いbcryptで作ったハッシュ(strength 4)を、同じ方式の名前で保存してあるとする
        String old = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("password123");
        System.out.println("古いハッシュ(strength 4)の先頭: " + old.substring(0, 18) + " ...");
        System.out.println("古いハッシュでも、検証できるか: " + encoder.matches("password123", old));
        System.out.println("古いハッシュは、作り直すべきか(upgradeEncoding): " + encoder.upgradeEncoding(old));
        System.out.println("新しいハッシュは、作り直すべきか: " + encoder.upgradeEncoding(newHash));

        // 方式の名前を持たない、昔の形式のハッシュは、検証の方法が分からない
        String legacy = new BCryptPasswordEncoder(4).encode("password123");
        try {
            encoder.matches("password123", legacy);
        } catch (IllegalArgumentException e) {
            System.out.println("方式の名前がないハッシュ: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
