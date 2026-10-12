package example;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// MockBeanTest と同じ設定(同じ @MockitoBean)のテスト。コンテキストが再利用されるはず
@SpringBootTest
class MockBeanSameConfigTest {
    @MockitoBean
    Notifier notifier;

    @Autowired
    LoanService service;

    @Test
    void 同じ設定のコンテキストを使う() {
        assertNotNull(service);
    }
}
