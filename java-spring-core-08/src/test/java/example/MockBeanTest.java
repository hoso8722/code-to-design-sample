package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Notifier のBeanを、モックに差し替えたテスト
@SpringBootTest
class MockBeanTest {
    @MockitoBean
    Notifier notifier;

    @Autowired
    LoanService service;

    @Test
    void 通知がモックに渡る() {
        when(notifier.notifyLoan("sora", "こころ")).thenReturn("モックの返事");
        assertEquals("モックの返事", service.lend("sora", "こころ"));
        verify(notifier).notifyLoan("sora", "こころ");
    }
}
