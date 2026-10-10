package example.books;

import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 認証なしで呼べる、公開のURI
@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final LoanProperties loan;

    public PublicController(LoanProperties loan) {
        this.loan = loan;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    // 設定から読んだ値を返す(公開してよい値だけ)
    @GetMapping("/policy")
    public Map<String, Object> policy() {
        return Map.of("libraryName", loan.libraryName(), "defaultDays", loan.defaultDays());
    }

    // 処理に時間がかかる要求を模擬する(停止の実験用)
    @GetMapping("/slow")
    public Map<String, String> slow(@RequestParam(defaultValue = "3") int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
        return Map.of("result", "finished after " + seconds + "s");
    }
}
