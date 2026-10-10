package example.books;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ログの出力を確かめるための、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class LoggingDemoController {
    private static final Logger log = LoggerFactory.getLogger(LoggingDemoController.class);

    // 同じ内容を、loggerとSystem.out.printlnで書く
    @GetMapping("/levels")
    public Map<String, String> levels() {
        log.trace("TRACE:処理の細かい手順");
        log.debug("DEBUG:開発中に調べたい情報");
        log.info("INFO:業務の区切りとなる出来事");
        log.warn("WARN:異常ではないが、気をつけたい状況");
        log.error("ERROR:処理が失敗した");
        System.out.println("println:System.out.printlnで書いた行");
        return Map.of("status", "ok");
    }
}
