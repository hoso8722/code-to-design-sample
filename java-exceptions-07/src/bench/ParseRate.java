// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// 1,000個の文字列を点数に変換する時間。failurePercent % の文字列は、数値にできない
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class ParseRate {
    @Param({"0", "1", "10", "50"})
    int failurePercent;

    String[] texts;

    @Setup
    public void setUp() {
        Random random = new Random(1);
        texts = new String[1_000];
        for (int i = 0; i < texts.length; i++) {
            boolean invalid = random.nextInt(100) < failurePercent;
            texts[i] = invalid ? "八十" : String.valueOf(random.nextInt(101));
        }
    }

    // 変換を試して、例外を捕まえる
    @Benchmark
    public int tryAndCatch() {
        int invalid = 0;
        for (String text : texts) {
            try {
                Integer.parseInt(text);
            } catch (NumberFormatException e) {
                invalid++;
            }
        }
        return invalid;
    }

    // 先に数字だけかどうかを確かめてから、変換する
    @Benchmark
    public int checkFirst() {
        int invalid = 0;
        for (String text : texts) {
            if (isDigits(text)) {
                Integer.parseInt(text);
            } else {
                invalid++;
            }
        }
        return invalid;
    }

    static boolean isDigits(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
