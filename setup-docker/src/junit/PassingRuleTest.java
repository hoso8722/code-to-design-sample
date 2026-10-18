import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PassingRuleTest {
    private final PassingRule rule = new PassingRule(60);

    @Test
    void 合計が合格ラインと同じなら合格() {
        assertTrue(rule.passes(60));
    }

    @Test
    void 合計が合格ラインより低ければ不合格() {
        assertFalse(rule.passes(59));
    }

    @Test
    void 合計が合格ラインより高ければ合格() {
        assertTrue(rule.passes(61));
    }
}
