package exam;

import java.util.ArrayList;
import java.util.List;

// 1つのメソッドに、検証がすべて書かれている
public class Validation {
    public static List<String> validate(Form form) {
        List<String> errors = new ArrayList<>();
    if (form.name() == null || form.name().isBlank()) {
        errors.add("名前が空です");
    }
    if (form.email() == null || !form.email().contains("@")) {
        errors.add("メールアドレスの形式が不正です");
    }
    if (form.math() < 0 || form.math() > 100 || form.english() < 0 || form.english() > 100 || form.science() < 0 || form.science() > 100) {
        errors.add("点数が0〜100の範囲外です");
    }
    if (form.name() != null && form.name().chars().anyMatch(c -> "<>&".indexOf(c) >= 0)) {
        errors.add("名前に使えない文字があります");
    }
        return errors;
    }
}
