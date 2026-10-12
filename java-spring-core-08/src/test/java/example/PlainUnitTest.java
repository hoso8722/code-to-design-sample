package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Springを起動しない、普通の単体テスト
class PlainUnitTest {
    @Test
    void 貸出の通知が作られる() {
        Notifier fake = (user, title) -> "偽物:" + user;
        assertEquals("偽物:sora", new LoanService(fake).lend("sora", "こころ"));
    }
}
