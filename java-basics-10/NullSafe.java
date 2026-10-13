// 実行: java NullSafe.java
import java.util.Objects;
import java.util.Optional;

public class NullSafe {
    static String findNickname(String name) {
        return "田中".equals(name) ? "たなっち" : null;    // 見つからないときは null
    }

    public static void main(String[] args) {
        String nickname = findNickname("鈴木");
        System.out.println("そのまま使うと NullPointerException になる値: " + nickname);

        // 1. null の確認
        System.out.println("null の確認: " + (nickname != null ? nickname : "(なし)"));

        // 2. Optional で「ないかもしれない」ことを型で表す
        Optional<String> optional = Optional.ofNullable(findNickname("田中"));
        System.out.println("Optional: " + optional.orElse("(なし)"));
        System.out.println("Optional(なし): " + Optional.ofNullable(findNickname("鈴木")).orElse("(なし)"));

        // 3. 引数が null でないことを、最初に確かめる
        try {
            Objects.requireNonNull(nickname, "nickname は必須です");
        } catch (NullPointerException e) {
            System.out.println("requireNonNull: " + e.getMessage());
        }
    }
}
