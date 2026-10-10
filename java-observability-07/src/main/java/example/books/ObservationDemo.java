package example.books;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Observationの動きを確かめる、実験用のエンドポイント
@RestController
@RequestMapping("/api/demo")
public class ObservationDemo {
    private static final Logger log = LoggerFactory.getLogger(ObservationDemo.class);

    private final ObservationRegistry registry;

    public ObservationDemo(ObservationRegistry registry) {
        this.registry = registry;
    }

    // 1つの処理を Observation で包む。名前と、低カーディナリティ / 高カーディナリティのキーを付ける
    @GetMapping("/observe")
    public Map<String, String> observe(@RequestParam String user) throws InterruptedException {
        Observation.createNotStarted("demo.checkout", registry)
                .lowCardinalityKeyValue("channel", "web")
                .highCardinalityKeyValue("user.id", user)
                .observe(() -> {
                    log.info("処理の中のログ");
                    sleep(50);
                });
        return Map.of("status", "ok");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
