package example.books;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    record BookView(long id, String title) {}

    private final List<BookView> books = List.of(
            new BookView(1, "吾輩は猫である"),
            new BookView(2, "坊っちゃん"));

    @GetMapping
    public List<BookView> list() {
        return books;
    }

    @GetMapping("/{id}")
    public BookView get(@PathVariable long id) {
        return books.stream().filter(b -> b.id() == id).findFirst().orElse(null);
    }
}
