// 実行: mvn test
// 会員。メールアドレスは一意
public record Member(long id, String email, String displayName) {}
