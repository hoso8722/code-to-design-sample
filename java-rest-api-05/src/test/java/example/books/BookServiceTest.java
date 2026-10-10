package example.books;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Springを起動しない単体テスト。業務のルールだけを確かめる
class BookServiceTest {
    private BookService service;

    @BeforeEach
    void setUp() {
        // コンストラクターで受け取る作りなので、new で組み立てられる
        service = new BookService(new BookStore());
    }

    @Test
    void 登録した本をIDで探せる() {
        BookEntity saved = service.register("こころ", 4);

        assertEquals("こころ", service.find(saved.id()).title());
    }

    @Test
    void 同じ題名は登録できない() {
        service.register("こころ", 4);

        assertThrows(DuplicateTitleException.class, () -> service.register("こころ", 1));
    }

    @Test
    void 存在しないIDは見つからない() {
        assertThrows(BookNotFoundException.class, () -> service.find(999));
    }

    @Test
    void 部分更新は送った項目だけを変える() {
        BookEntity saved = service.register("こころ", 4);

        BookEntity patched = service.patch(saved.id(), null, 9);

        assertEquals("こころ", patched.title());
        assertEquals(9, patched.copies());
    }
}
