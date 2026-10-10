package example.books;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

// 本をメモリに保存する(まだデータベースは使わない)
@Component
public class BookStore {
    private final Map<Long, BookEntity> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public BookStore() {
        save("吾輩は猫である", 3);
        save("坊っちゃん", 2);
    }

    public BookEntity save(String title, int copies) {
        BookEntity book = new BookEntity(nextId.getAndIncrement(), title, copies, "");
        books.put(book.id(), book);
        return book;
    }

    public Optional<BookEntity> find(long id) {
        return Optional.ofNullable(books.get(id));
    }

    public Optional<BookEntity> findByTitle(String title) {
        return books.values().stream().filter(b -> b.title().equals(title)).findFirst();
    }

    public List<BookEntity> findAll() {
        return books.values().stream().sorted((a, b) -> Long.compare(a.id(), b.id())).toList();
    }

    // IDが存在すれば置き換えて true、存在しなければ false
    public boolean replace(long id, String title, int copies) {
        return books.computeIfPresent(id, (k, old) -> new BookEntity(id, title, copies, old.internalMemo())) != null;
    }

    public boolean delete(long id) {
        return books.remove(id) != null;
    }
}
