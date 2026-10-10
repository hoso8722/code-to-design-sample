package example.books;

import org.springframework.stereotype.Service;

// 改善した例:途中の層は記録せず、文脈を足した例外に包んで投げる。記録は、境界で1回だけ行う
@Service
public class BoundaryLoggingService {
    private final DemoStore store;

    public BoundaryLoggingService(DemoStore store) {
        this.store = store;
    }

    public void register(String title) {
        try {
            store.save(title);
        } catch (DemoStorageException e) {
            // 何をしようとして失敗したかを足して、元の例外を原因として残す
            throw new BookRegistrationException("本の登録に失敗した title=" + title, e);
        }
    }
}
