// 実行: mvn test
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// データベースを使わない単体テスト。Dockerは要らない
class EmailsTest {
    @Test
    void 前後の空白を除き_小文字にそろえる() {
        assertEquals("sora@example.com", Emails.normalize("  Sora@Example.COM "));
    }
}
