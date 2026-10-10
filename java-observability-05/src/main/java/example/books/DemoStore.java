package example.books;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 保存の層(実験用)。呼ぶと、必ず失敗する
@Component
public class DemoStore {
    private static final Logger log = LoggerFactory.getLogger(DemoStore.class);

    // 失敗を記録してから、投げ直す(重複して記録する例で使う)
    public void saveAndLog(String title) {
        try {
            throw new IOException("ディスクの空きがありません");
        } catch (IOException e) {
            log.error("保存に失敗した title={}", title, e);
            throw new DemoStorageException("保存に失敗した", e);
        }
    }

    // 記録せずに、例外を投げるだけ(境界で1回だけ記録する例で使う)
    public void save(String title) {
        try {
            throw new IOException("ディスクの空きがありません");
        } catch (IOException e) {
            throw new DemoStorageException("保存に失敗した", e);
        }
    }
}
