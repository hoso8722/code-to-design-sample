// 実行: mvn test
// 同じメールアドレスの会員がすでにいる
public class DuplicateEmailException extends Exception {
    public DuplicateEmailException(String email, Throwable cause) {
        super("メールアドレスが登録済みです: " + email, cause);
    }
}
