package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// LoanService もモックにした、別の設定のテスト。新しいコンテキストが要る
@SpringBootTest
class MockBeanOtherConfigTest {
    @MockitoBean
    Notifier notifier;

    @MockitoBean
    LoanService service;

    @Autowired
    LoanService injected;

    @Test
    void 別の設定のコンテキストを使う() {
        assertNotNull(injected);
    }
}
