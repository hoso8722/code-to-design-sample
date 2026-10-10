package example.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// よくない例:各層が、例外を記録してから、投げ直す
@Service
public class DuplicateLoggingService {
    private static final Logger log = LoggerFactory.getLogger(DuplicateLoggingService.class);

    private final DemoStore store;

    public DuplicateLoggingService(DemoStore store) {
        this.store = store;
    }

    public void register(String title) {
        try {
            store.saveAndLog(title);
        } catch (DemoStorageException e) {
            log.error("本の登録に失敗した title={}", title, e);
            throw e;
        }
    }
}
