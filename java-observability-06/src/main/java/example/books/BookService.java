package example.books;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    // loggerは、クラスごとに1つ作る。名前にはクラスの完全修飾名が使われる(example.books.BookService)
    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookStore store;
    private final MeterRegistry registry;
    private final Timer loanTimer;

    public BookService(BookStore store, MeterRegistry registry) {
        this.store = store;
        this.registry = registry;
        this.loanTimer = Timer.builder("books.loan.duration").description("貸出の処理時間").register(registry);
        // 現在の在庫の合計。測るたびに、この関数が呼ばれる
        Gauge.builder("books.stock", store, s -> s.findAll().stream().mapToInt(BookEntity::copies).sum())
                .description("在庫の合計(冊)")
                .register(registry);
    }

    public List<BookEntity> list() {
        return store.findAll();
    }

    public BookEntity find(long id) {
        log.debug("本を探す id={}", id);
        return store.find(id).orElseThrow(() -> {
            // 見つからないのは通常の結果なので、DEBUGにとどめる
            log.debug("本が見つからない id={}", id);
            return new BookNotFoundException(id);
        });
    }

    public BookEntity register(String title, int copies) {
        log.trace("登録の処理を始める");
        if (store.findByTitle(title).isPresent()) {
            log.warn("題名が重複しているため、登録しない title={}", title);
            throw new DuplicateTitleException(title);
        }
        BookEntity saved = store.save(title, copies);
        log.info("本を登録した id={} copies={}", saved.id(), copies);
        return saved;
    }

    public void replace(long id, String title, int copies) {
        if (!store.replace(id, title, copies)) {
            throw new BookNotFoundException(id);
        }
    }

    // 送った項目(null でないもの)だけを変える
    public BookEntity patch(long id, String title, Integer copies) {
        BookEntity current = find(id);
        store.replace(id, title != null ? title : current.title(), copies != null ? copies : current.copies());
        return find(id);
    }

    public void delete(long id) {
        if (!store.delete(id)) {
            throw new BookNotFoundException(id);
        }
        log.info("本を削除した id={}", id);
    }

    // 貸出:結果ごとに回数を数え、処理時間を測る
    public BookEntity borrow(long id, String borrower) {
        String result = "error";
        try {
            BookEntity book = loanTimer.recordCallable(() -> doBorrow(id, borrower));
            result = "success";
            return book;
        } catch (BookUnavailableException e) {
            result = "out_of_stock";
            throw e;
        } catch (BookNotFoundException e) {
            result = "not_found";
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            registry.counter("books.loans", "result", result).increment();
        }
    }

    // 貸出:在庫を1冊減らす
    private BookEntity doBorrow(long id, String borrower) {
        log.trace("貸出の処理を始める id={}", id);
        BookEntity current = find(id);
        if (current.copies() == 0) {
            log.warn("在庫がないため、貸し出せない id={}", id);
            throw new BookUnavailableException(id);
        }
        if ("error".equals(borrower)) {
            throw new IllegalStateException("貸出の記録の作成に失敗した(模擬)");
        }
        store.replace(id, current.title(), current.copies() - 1);
        // fluent API:メッセージと、検索しやすい key-value を分けて渡す
        log.atInfo()
                .addKeyValue("bookId", id)
                .addKeyValue("copiesLeft", current.copies() - 1)
                .log("貸し出した");
        return find(id);
    }
}
