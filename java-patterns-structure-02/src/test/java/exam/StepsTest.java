package exam;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// 第1回の特性テストの表を、手順0〜6のすべてに対して実行する
class StepsTest {
    interface Judge { String apply(String name, int m, int e, int s, String type); }

    static Stream<Arguments> steps() {
        return Stream.of(
            Arguments.of("手順0", (Judge) Step0::judge),
            Arguments.of("手順1", (Judge) Step1::judge),
            Arguments.of("手順2", (Judge) Step2::judge),
            Arguments.of("手順3", (Judge) Step3::judge),
            Arguments.of("手順4", (Judge) Step4::judge),
            Arguments.of("手順5", (Judge) Step5::judge),
            Arguments.of("手順6", (Judge) Step6::judge));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("steps")
    void 全件が現在の動きと一致する(String step, Judge judge) {
        for (Cases.Case c : Cases.ALL) {
            assertEquals(c.expected(), judge.apply(c.name(), c.math(), c.english(), c.science(), c.type()),
                step + "：" + c.label());
        }
    }
}
