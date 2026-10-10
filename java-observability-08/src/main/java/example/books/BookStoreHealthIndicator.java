package example.books;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

// 本の保存先の状態を、ヘルスチェックに加える。実験では、フラグで故障を模擬する
@Component
public class BookStoreHealthIndicator implements HealthIndicator {
    private final AtomicBoolean down = new AtomicBoolean(false);

    void setDown(boolean value) {
        down.set(value);
    }

    @Override
    public Health health() {
        if (down.get()) {
            return Health.down().withDetail("reason", "保存先に接続できない(模擬)").build();
        }
        return Health.up().withDetail("books", "ok").build();
    }
}
