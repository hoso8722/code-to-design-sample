package example.books;

import java.util.Map;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// ヘルスチェックの変化を確かめる、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class HealthDemoController {
    private final BookStoreHealthIndicator bookStore;
    private final ApplicationContext context;

    public HealthDemoController(BookStoreHealthIndicator bookStore, ApplicationContext context) {
        this.bookStore = bookStore;
        this.context = context;
    }

    @GetMapping("/slow")
    public Map<String, Object> slow() throws InterruptedException {
        Thread.sleep(3000);
        return Map.of("done", true);
    }

    // 保存先の故障を模擬する
    @GetMapping("/store-down")
    public Map<String, Object> storeDown(@RequestParam boolean value) {
        bookStore.setDown(value);
        return Map.of("storeDown", value);
    }

    // トラフィックの受け付けを止める(終了処理と同じ状態にする)
    @GetMapping("/refuse-traffic")
    public Map<String, Object> refuseTraffic(@RequestParam boolean value) {
        AvailabilityChangeEvent.publish(context, value ? ReadinessState.REFUSING_TRAFFIC : ReadinessState.ACCEPTING_TRAFFIC);
        return Map.of("refuseTraffic", value);
    }
}
