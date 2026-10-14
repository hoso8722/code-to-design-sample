package exam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 検証の部品を、順に連ねる
public class Validation {
    private static final List<Validator> CHAIN = List.of(
    form -> (form.name() == null || form.name().isBlank()) ? Optional.of("名前が空です") : Optional.empty(),
    form -> (form.email() == null || !form.email().contains("@")) ? Optional.of("メールアドレスの形式が不正です") : Optional.empty(),
    form -> (form.math() < 0 || form.math() > 100 || form.english() < 0 || form.english() > 100 || form.science() < 0 || form.science() > 100) ? Optional.of("点数が0〜100の範囲外です") : Optional.empty(),
    form -> (form.name() != null && form.name().chars().anyMatch(c -> "<>&".indexOf(c) >= 0)) ? Optional.of("名前に使えない文字があります") : Optional.empty()
    );

    public static List<String> validate(Form form) {
        List<String> errors = new ArrayList<>();
        for (Validator validator : CHAIN) {
            validator.check(form).ifPresent(errors::add);
        }
        return errors;
    }
}
