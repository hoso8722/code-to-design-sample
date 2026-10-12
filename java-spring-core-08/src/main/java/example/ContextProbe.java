package example;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

// コンテキストが作られるたびに、1行を出力する(コンテキストの数を、数えるため)
@Component
public class ContextProbe {
    private static final AtomicInteger created = new AtomicInteger();

    @PostConstruct
    void announce() {
        System.out.println("[コンテキスト生成] " + created.incrementAndGet() + "個目");
    }
}
