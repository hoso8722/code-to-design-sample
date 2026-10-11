package example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class SlowTest {
    @Test
    void 時間のかかる確認() throws InterruptedException {
        Thread.sleep(1000);
        assertEquals(7, new Calculator().add(3, 4));
    }
}
