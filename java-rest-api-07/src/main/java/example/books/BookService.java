package example.books;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    // 並べ替えに使える列。利用者の入力は、ここにある値にだけ対応づける
    private static final Map<String, String> SORT_COLUMNS = Map.of("id", "id", "title", "title", "copies", "copies");

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<BookEntity> list(String sort) {
        String column = SORT_COLUMNS.get(sort);
        if (column == null) {
            throw new IllegalArgumentException("並べ替えの項目が不正です: " + sort);
        }
        return repository.findAll(column);
    }

    public BookEntity find(long id) {
        return repository.find(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public BookEntity register(String title, int copies) {
        if (repository.findByTitle(title).isPresent()) {
            throw new DuplicateTitleException(title);
        }
        return repository.insert(title, copies);
    }

    public void replace(long id, String title, int copies) {
        if (!repository.update(id, title, copies)) {
            throw new BookNotFoundException(id);
        }
    }

    public BookEntity patch(long id, String title, Integer copies) {
        BookEntity current = find(id);
        repository.update(id, title != null ? title : current.title(), copies != null ? copies : current.copies());
        return find(id);
    }

    public void delete(long id) {
        if (!repository.delete(id)) {
            throw new BookNotFoundException(id);
        }
    }

    // 貸出:在庫を1冊減らし、貸出の記録を作る。2つの更新は、1つのトランザクションで行う
    @Transactional
    public void borrow(long bookId, String borrower) {
        if (repository.decrementCopies(bookId) == 0) {
            throw new BookUnavailableException(bookId);
        }
        if ("error".equals(borrower)) {
            throw new IllegalStateException("貸出の記録の作成に失敗した(模擬)"); // 実行時例外
        }
        repository.insertLoan(bookId, borrower);
    }

    // 検査例外を投げる版。@Transactional の既定のままでは、検査例外ではロールバックされない
    @Transactional
    public void borrowChecked(long bookId, String borrower) throws LoanRecordException {
        if (repository.decrementCopies(bookId) == 0) {
            throw new BookUnavailableException(bookId);
        }
        if ("error".equals(borrower)) {
            throw new LoanRecordException("貸出の記録の作成に失敗した(模擬)");
        }
        repository.insertLoan(bookId, borrower);
    }

    // 検査例外でもロールバックするよう、rollbackFor で指定した版
    @Transactional(rollbackFor = LoanRecordException.class)
    public void borrowCheckedSafely(long bookId, String borrower) throws LoanRecordException {
        borrowCheckedBody(bookId, borrower);
    }

    private void borrowCheckedBody(long bookId, String borrower) throws LoanRecordException {
        if (repository.decrementCopies(bookId) == 0) {
            throw new BookUnavailableException(bookId);
        }
        if ("error".equals(borrower)) {
            throw new LoanRecordException("貸出の記録の作成に失敗した(模擬)");
        }
        repository.insertLoan(bookId, borrower);
    }
}
