package example.books;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

// よくない例:依存をフィールドに直接注入する作りは、Springなしでは依存が入らない
class FieldInjectionTest {
    static class FieldInjectedService {
        @Autowired
        private BookStore store; // Springがリフレクションで直接書き込む。new しただけでは null のまま

        BookEntity find(long id) {
            return store.find(id).orElseThrow(() -> new BookNotFoundException(id));
        }
    }

    @Test
    void フィールド注入の作りは_newしただけでは依存が入らない() {
        FieldInjectedService service = new FieldInjectedService();

        assertThrows(NullPointerException.class, () -> service.find(1));
    }
}
