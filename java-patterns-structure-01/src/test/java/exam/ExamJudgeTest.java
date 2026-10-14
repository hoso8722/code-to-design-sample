package exam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

// 特性テスト：現在の動きを、そのまま期待値にして固定する
class ExamJudgeTest {
    @ParameterizedTest(name = "{0}")
    @FieldSource("exam.Cases#ALL")
    void 現在の動きが変わっていない(Cases.Case c) {
        assertEquals(c.expected(), ExamJudge.judge(c.name(), c.math(), c.english(), c.science(), c.type()));
    }
}
