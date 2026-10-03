// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)        // 1回あたりの平均時間を計る
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)       // 1秒 × 5回、準備運動（結果には含めない）
@Measurement(iterations = 5, time = 1)  // 1秒 × 5回、計測する
@Fork(3)                                // 新しいJVMを3回起動して、それぞれで計る
@State(Scope.Thread)
public class StringConcatBench {
    @Param({"10", "100", "1000"})       // つなぐ数を変えて計る
    int n;

    @Benchmark
    public String plusLoop() {          // 戻り値は JMH が使ってくれる（計算が消されない）
        String s = "";
        for (int i = 0; i < n; i++) s += i;
        return s;
    }

    @Benchmark
    public String builder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i);
        return sb.toString();
    }

    @Benchmark
    public String joining() {
        return IntStream.range(0, n).mapToObj(Integer::toString).collect(Collectors.joining());
    }

    @Benchmark
    public void deadCode() {            // 結果を使わない（悪い例）
        long x = (long) n * n % 7;
    }

    @Benchmark
    public void blackhole(Blackhole bh) {  // 結果を Blackhole に渡す（良い例）
        bh.consume((long) n * n % 7);
    }
}
