package example.books;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
public class BookController {
    // 受け取るデータ:登録のとき、利用者が送ってよい項目だけ
    public record CreateBookRequest(String title, int copies) {}

    // 返すデータ:公開してよい項目だけ
    public record BookResponse(long id, String title, int copies) {}

    private final BookStore store;

    public BookController(BookStore store) {
        this.store = store;
    }

    private static BookResponse toResponse(BookEntity book) {
        return new BookResponse(book.id(), book.title(), book.copies());
    }

    // クエリ文字列(?titleContains=...&limit=...)は @RequestParam で受け取る
    @GetMapping
    public List<BookResponse> search(@RequestParam(required = false) String titleContains,
                                     @RequestParam(defaultValue = "10") int limit) {
        return store.search(titleContains, limit).stream().map(BookController::toResponse).toList();
    }

    // URIの一部(/api/books/{id})は @PathVariable で受け取る
    @GetMapping("/{id}")
    public BookResponse get(@PathVariable long id) {
        return store.find(id).map(BookController::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // リクエストの本文(JSON)は @RequestBody で受け取る
    @PostMapping
    public BookResponse create(@RequestBody CreateBookRequest request) {
        BookEntity saved = store.save(request.title(), request.copies(), "");
        return toResponse(saved);
    }
}
