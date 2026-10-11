package example.broken;

import static org.junit.jupiter.api.Assertions.assertEquals;

import example.Calculator;
import org.junit.jupiter.api.Test;

// わざと失敗するテスト。通常のビルドでは除外している(pom.xml の excludes)
class BrokenTest {
    @Test
    void 誤った期待値() {
        assertEquals(6, new Calculator().add(2, 3), "2 + 3 の結果");
    }
}
