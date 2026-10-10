package example.books;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// MDCの動きを確かめるための、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class MdcDemoController {
    private static final Logger log = LoggerFactory.getLogger(MdcDemoController.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    // 少し時間のかかる処理。複数のリクエストが同時に来ると、ログの行が混ざる
    @GetMapping("/work")
    public Map<String, String> work(@RequestParam String name) throws InterruptedException {
        log.info("処理を始める name={}", name);
        Thread.sleep(300);
        log.info("処理を終える name={}", name);
        return Map.of("status", "ok");
    }

    // 別のスレッドで書いたログに、requestIdが付くかを比べる
    @GetMapping("/mdc-thread")
    public Map<String, String> mdcThread() throws Exception {
        log.info("リクエストのスレッドで処理する");
        executor.submit(() -> log.info("別のスレッドで処理する(そのまま)")).get();
        var copied = MDC.getCopyOfContextMap();
        executor.submit(() -> {
            MDC.setContextMap(copied);
            try {
                log.info("別のスレッドで処理する(MDCを引き継ぐ)");
            } finally {
                MDC.clear();
            }
        }).get();
        return Map.of("status", "ok");
    }

    // 取り除かずに入れっぱなしにする(よくない例)
    @GetMapping("/mdc-leak/set")
    public Map<String, String> leakSet(@RequestParam String value) {
        MDC.put("userId", value);
        log.info("userIdをMDCに入れた");
        return Map.of("status", "ok");
    }

    // 別のリクエストが、同じスレッドに当たったときの見え方を確かめる
    @GetMapping("/mdc-leak/check")
    public Map<String, String> leakCheck() {
        log.info("別のリクエストの処理");
        return Map.of("userId", String.valueOf(MDC.get("userId")));
    }
}
