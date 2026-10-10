package example.books;

import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    public record CreateBookRequest(String title, int copies) {}

    // 置き換え(PUT):すべての項目を送る
    public record ReplaceBookRequest(String title, int copies) {}

    // 部分更新(PATCH):送った項目だけを変える。送らない項目は null
    public record PatchBookRequest(String title, Integer copies) {}

    public record BookResponse(long id, String title, int copies) {}

    private final BookStore store;

    public BookController(BookStore store) {
        this.store = store;
    }

    private static BookResponse toResponse(BookEntity book) {
        return new BookResponse(book.id(), book.title(), book.copies());
    }

    // 取得(一覧):常に200。0件でも空の配列を返す
    @GetMapping
    public List<BookResponse> list() {
        return store.findAll().stream().map(BookController::toResponse).toList();
    }

    // 取得(1冊):あれば200、なければ404
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> get(@PathVariable long id) {
        return store.find(id).map(b -> ResponseEntity.ok(toResponse(b)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 作成(POST):201と、作った本のURIを Location ヘッダーで返す。同じ題名があれば409
    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody CreateBookRequest request) {
        if (store.findByTitle(request.title()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        BookEntity saved = store.save(request.title(), request.copies());
        return ResponseEntity.created(URI.create("/api/books/" + saved.id())).body(toResponse(saved));
    }

    // 置き換え(PUT):あれば204(本文なし)、なければ404
    @PutMapping("/{id}")
    public ResponseEntity<Void> replace(@PathVariable long id, @RequestBody ReplaceBookRequest request) {
        return store.replace(id, request.title(), request.copies())
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // 部分更新(PATCH):変えたあとの本を200で返す。なければ404
    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> patch(@PathVariable long id, @RequestBody PatchBookRequest request) {
        return store.find(id).map(current -> {
            String title = request.title() != null ? request.title() : current.title();
            int copies = request.copies() != null ? request.copies() : current.copies();
            store.replace(id, title, copies);
            return ResponseEntity.ok(new BookResponse(id, title, copies));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 削除(DELETE):削除できたら204、なければ404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return store.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
