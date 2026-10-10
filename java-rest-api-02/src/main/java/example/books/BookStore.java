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
        save("吾輩は猫である", 3, "社内メモ:初版は旧仮名づかい");
        save("坊っちゃん", 2, "社内メモ:貸出が多い");
    }

    public BookEntity save(String title, int copies, String internalMemo) {
        BookEntity book = new BookEntity(nextId.getAndIncrement(), title, copies, internalMemo);
        books.put(book.id(), book);
        return book;
    }

    public Optional<BookEntity> find(long id) {
        return Optional.ofNullable(books.get(id));
    }

    public List<BookEntity> search(String titleContains, int limit) {
        return books.values().stream()
                .filter(b -> titleContains == null || b.title().contains(titleContains))
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .limit(limit)
                .toList();
    }
}
