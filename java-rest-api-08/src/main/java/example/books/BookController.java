package example.books;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// HTTPの要求と応答に変換するだけ。業務の判断は BookService に任せる
@RestController
@RequestMapping("/api/books")
public class BookController {
    public record CreateBookRequest(
            @NotBlank(message = "題名は必須です") @Size(max = 120, message = "題名は120文字以内にしてください") String title,
            @Min(value = 0, message = "冊数は0以上にしてください") @Max(value = 1000, message = "冊数は1000以下にしてください") int copies) {}

    public record ReplaceBookRequest(
            @NotBlank(message = "題名は必須です") @Size(max = 120, message = "題名は120文字以内にしてください") String title,
            @Min(value = 0, message = "冊数は0以上にしてください") @Max(value = 1000, message = "冊数は1000以下にしてください") int copies) {}

    public record PatchBookRequest(
            @Size(min = 1, max = 120, message = "題名は1〜120文字にしてください") String title,
            @Min(value = 0, message = "冊数は0以上にしてください") @Max(value = 1000, message = "冊数は1000以下にしてください") Integer copies) {}

    public record BookResponse(long id, String title, int copies) {}

    public record LoanRequest(@NotBlank(message = "借りる人の名前は必須です") String borrower) {}

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    private static BookResponse toResponse(BookEntity book) {
        return new BookResponse(book.id(), book.title(), book.copies());
    }

    // sort は、並べ替えの項目(id、title、copies)。許可していない値は 400
    @GetMapping
    public List<BookResponse> list(@RequestParam(defaultValue = "id") String sort) {
        return service.list(sort).stream().map(BookController::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable @Min(value = 1, message = "IDは1以上にしてください") long id) {
        return toResponse(service.find(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        BookEntity saved = service.register(request.title(), request.copies());
        return ResponseEntity.created(URI.create("/api/books/" + saved.id())).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> replace(@PathVariable long id, @Valid @RequestBody ReplaceBookRequest request) {
        service.replace(id, request.title(), request.copies());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public BookResponse patch(@PathVariable long id, @Valid @RequestBody PatchBookRequest request) {
        return toResponse(service.patch(id, request.title(), request.copies()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 貸出:在庫を減らし、記録を作る(実行時例外の版)
    @PostMapping("/{id}/loans")
    public ResponseEntity<Void> borrow(@PathVariable long id, @Valid @RequestBody LoanRequest request) {
        service.borrow(id, request.borrower());
        return ResponseEntity.status(201).build();
    }

    // 貸出(検査例外の版。@Transactional の既定のまま)
    @PostMapping("/{id}/loans-checked")
    public ResponseEntity<Void> borrowChecked(@PathVariable long id, @Valid @RequestBody LoanRequest request)
            throws LoanRecordException {
        service.borrowChecked(id, request.borrower());
        return ResponseEntity.status(201).build();
    }

    // 貸出(検査例外の版。rollbackFor を指定)
    @PostMapping("/{id}/loans-checked-safe")
    public ResponseEntity<Void> borrowCheckedSafely(@PathVariable long id, @Valid @RequestBody LoanRequest request)
            throws LoanRecordException {
        service.borrowCheckedSafely(id, request.borrower());
        return ResponseEntity.status(201).build();
    }
}
