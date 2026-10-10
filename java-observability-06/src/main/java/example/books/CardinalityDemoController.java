package example.books;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// タグの値の種類(カーディナリティ)が、メトリクスの数に与える影響を確かめる、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class CardinalityDemoController {
    private final MeterRegistry registry;

    public CardinalityDemoController(MeterRegistry registry) {
        this.registry = registry;
    }

    // bad: 利用者のIDをタグにする / good: 結果の種類だけをタグにする
    @GetMapping("/cardinality")
    public Map<String, Object> cardinality(@RequestParam String mode, @RequestParam int n) {
        long before = registry.getMeters().size();
        for (int i = 0; i < n; i++) {
            if (mode.equals("bad")) {
                registry.counter("demo.requests.bad", "userId", "user-" + i).increment();
            } else {
                registry.counter("demo.requests.good", "result", i % 2 == 0 ? "success" : "failure").increment();
            }
        }
        return Map.of("mode", mode, "requests", n, "metersBefore", before, "metersAfter", registry.getMeters().size());
    }
}
