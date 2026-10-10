package example.books;

import java.util.List;
import org.springframework.stereotype.Service;

// 本に対する業務の操作。HTTPのことは知らない
@Service
public class BookService {
    private final BookStore store;

    // 依存するものは、コンストラクターで受け取る
    public BookService(BookStore store) {
        this.store = store;
    }

    public List<BookEntity> list() {
        return store.findAll();
    }

    public BookEntity find(long id) {
        return store.find(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookEntity register(String title, int copies) {
        if (store.findByTitle(title).isPresent()) {
            throw new DuplicateTitleException(title);
        }
        return store.save(title, copies);
    }

    public void replace(long id, String title, int copies) {
        if (!store.replace(id, title, copies)) {
            throw new BookNotFoundException(id);
        }
    }

    // 送った項目(null でないもの)だけを変える
    public BookEntity patch(long id, String title, Integer copies) {
        BookEntity current = find(id);
        String newTitle = title != null ? title : current.title();
        int newCopies = copies != null ? copies : current.copies();
        store.replace(id, newTitle, newCopies);
        return find(id);
    }

    public void delete(long id) {
        if (!store.delete(id)) {
            throw new BookNotFoundException(id);
        }
    }
}
