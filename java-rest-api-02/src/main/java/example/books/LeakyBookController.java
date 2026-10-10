package example.books;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

// よくない例:保存するための型(エンティティ)を、そのまま応答にしている
@RestController
@RequestMapping("/api/leaky/books")
public class LeakyBookController {
    private final BookStore store;

    public LeakyBookController(BookStore store) {
        this.store = store;
    }

    @GetMapping("/{id}")
    public BookEntity get(@PathVariable long id) {
        return store.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
