package example.books;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    // toString() が何回呼ばれたかを数える値
    static class Probe {
        int calls;

        @Override
        public String toString() {
            calls++;
            return "probe";
        }

        // 引数の中で呼ぶ、重い処理を表すメソッド
        String compute() {
            calls++;
            return "probe";
        }
    }

    // DEBUGが無効のとき、3つの書き方で toString() が呼ばれる回数を比べる
    @GetMapping("/cost")
    public Map<String, Object> cost(@RequestParam(defaultValue = "1000") int n) {
        Probe concatenated = new Probe();
        Probe placeholder = new Probe();
        Probe called = new Probe();
        Probe supplied = new Probe();
        for (int i = 0; i < n; i++) {
            log.debug("値=" + concatenated);   // 文字列連結:DEBUGが無効でも、先に文字列が作られる
            log.debug("値={}", placeholder);   // プレースホルダー:DEBUGが無効なら、文字列は作られない
            log.debug("値={}", called.compute());   // 引数でメソッドを呼ぶ:DEBUGが無効でも、呼び出しは先に行われる
            log.atDebug().setMessage("値={}").addArgument(() -> supplied.toString()).log(); // Supplier:無効なら呼ばれない
        }
        return Map.of("debugEnabled", log.isDebugEnabled(), "n", n,
                "連結", concatenated.calls, "プレースホルダー", placeholder.calls,
                "プレースホルダー+メソッド呼び出し", called.calls, "Supplier", supplied.calls);
    }

    public record LoginRequest(String user, String password) {}

    // toString() で、パスワードを伏せた形にする
    public record MaskedLoginRequest(String user, String password) {
        @Override
        public String toString() {
            return "MaskedLoginRequest[user=" + user + ", password=****]";
        }
    }

    // ログインの要求を、3つの書き方で記録して比べる(認証の処理そのものは、ここでは行わない)
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        log.info("[悪い例] ログイン要求 {}", request);
        log.info("[IDだけ] ログイン要求 user={}", request.user());
        log.info("[伏せる] ログイン要求 {}", new MaskedLoginRequest(request.user(), request.password()));
        return Map.of("status", "ok");
    }
}
