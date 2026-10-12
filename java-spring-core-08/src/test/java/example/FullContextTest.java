package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// コンテキスト全体を起動するテスト(本物のLogNotifierを使う)
@SpringBootTest
class FullContextTest {
    @Autowired
    LoanService service;

    @Test
    void 本物の実装で動く() {
        assertEquals("[ログ] sora / こころ", service.lend("sora", "こころ"));
    }
}
