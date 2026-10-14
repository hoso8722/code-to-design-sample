// 検証しない: 複数ファイルの例。collect.sh で java errors/Errors.java として実行している
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 連ねた検証の1つが、例外を投げたとき
public class Errors {
    record Form(String name) {}

    interface Validator { Optional<String> check(Form form); }

    public static void main(String[] args) {
        List<Validator> chain = List.of(
            f -> f.name().isBlank() ? Optional.of("名前が空です") : Optional.empty(),
            f -> { throw new IllegalStateException("データベースに接続できません"); },
            f -> f.name().length() > 10 ? Optional.of("名前が長すぎます") : Optional.empty());
        Form form = new Form("");

        // 1. 例外をそのまま伝える：検証全体が中断し、最初の検証の結果も失われる
        try {
            List<String> errors = new ArrayList<>();
            for (Validator v : chain) v.check(form).ifPresent(errors::add);
            System.out.println("そのまま伝える: " + errors);
        } catch (IllegalStateException e) {
            System.out.println("そのまま伝える: 中断した（" + e.getMessage() + "）");
        }

        // 2. 例外を検証の失敗として扱う：すべての検証が最後まで走る
        List<String> errors = new ArrayList<>();
        for (Validator v : chain) {
            try {
                v.check(form).ifPresent(errors::add);
            } catch (RuntimeException e) {
                errors.add("検証できませんでした（" + e.getMessage() + "）");
            }
        }
        System.out.println("失敗として扱う: " + errors);
    }
}
