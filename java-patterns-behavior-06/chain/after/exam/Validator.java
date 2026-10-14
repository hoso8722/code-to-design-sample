package exam;

import java.util.Optional;

// 検証を1つずつの部品にする。問題があればメッセージを返す
@FunctionalInterface
public interface Validator {
    Optional<String> check(Form form);
}
