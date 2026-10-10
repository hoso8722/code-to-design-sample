package example.books;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 可用性の状態の変化を、ログに書く
@Component
public class AvailabilityLogger {
    private static final Logger log = LoggerFactory.getLogger(AvailabilityLogger.class);

    @EventListener
    public void onChange(AvailabilityChangeEvent<?> event) {
        log.info("可用性の状態が変わった: {}", event.getState());
    }
}
