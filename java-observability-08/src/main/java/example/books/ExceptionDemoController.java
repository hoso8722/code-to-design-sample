package example.books;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 例外の記録を確かめるための、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class ExceptionDemoController {
    private static final Logger log = LoggerFactory.getLogger(ExceptionDemoController.class);

    private final DuplicateLoggingService duplicateLogging;
    private final BoundaryLoggingService boundaryLogging;

    public ExceptionDemoController(DuplicateLoggingService duplicateLogging, BoundaryLoggingService boundaryLogging) {
        this.duplicateLogging = duplicateLogging;
        this.boundaryLogging = boundaryLogging;
    }

    // 同じ例外を、5つの書き方で記録する
    @GetMapping("/exception-styles")
    public Map<String, String> exceptionStyles() {
        Exception e = new IllegalStateException("在庫の更新に失敗した");
        long id = 3;
        log.error("[1:連結] 失敗: " + e);
        log.error("[2:getMessage] 失敗: {}", e.getMessage());
        log.error("[3:プレースホルダーに例外] 失敗: {}", e);
        log.error("[4:例外を最後の引数] 失敗 id={}", id, e);
        log.error("[5:例外だけ] 失敗", e);
        return Map.of("status", "ok");
    }

    @PostMapping("/register/duplicate-logging")
    public Map<String, String> registerDuplicateLogging(@RequestParam String title) {
        duplicateLogging.register(title);
        return Map.of("status", "ok");
    }

    @PostMapping("/register/boundary-logging")
    public Map<String, String> registerBoundaryLogging(@RequestParam String title) {
        boundaryLogging.register(title);
        return Map.of("status", "ok");
    }

    // 外部のライブラリの例外のメッセージに、機密の値が含まれてしまう場合
    @GetMapping("/secret-in-message")
    public Map<String, String> secretInMessage(@RequestParam(defaultValue = "false") boolean mask) {
        Exception e = new IllegalStateException("接続に失敗した url=jdbc:postgresql://db/app?user=app&password=abc123");
        if (mask) {
            log.error("[伏せた] {}", Redactor.mask(e.getMessage()));
        } else {
            log.error("[そのまま] {}", e.getMessage());
        }
        return Map.of("status", "ok");
    }
}
