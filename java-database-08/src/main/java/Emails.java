// 実行: mvn test
// データベースを使わない処理。通常の単体テストで確かめられる
public final class Emails {
    private Emails() {}

    // 前後の空白を除き、小文字にそろえる
    public static String normalize(String email) {
        return email.strip().toLowerCase();
    }
}
