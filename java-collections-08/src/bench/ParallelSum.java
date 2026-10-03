// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.openjdk.jmh.annotations.*;

// 0〜n−1 の数を「処理」して合計する。逐次（sequential）と並列（parallel）を比べる
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class ParallelSum {
    @Param({"1000", "100000", "10000000"})
    int n;

    @Param({"light", "heavy"})
    String work;

    // 1要素あたりの処理。light は掛け算1回、heavy は計算を100回くり返す
    static long process(int x, boolean heavy) {
        if (!heavy) return (long) x * x;
        long v = x;
        for (int i = 0; i < 100; i++) v = (v * 31 + i) % 1_000_003;
        return v;
    }

    @Benchmark
    public long sequential() {
        boolean heavy = work.equals("heavy");
        return IntStream.range(0, n).mapToLong(x -> process(x, heavy)).sum();
    }

    @Benchmark
    public long parallel() {
        boolean heavy = work.equals("heavy");
        return IntStream.range(0, n).parallel().mapToLong(x -> process(x, heavy)).sum();
    }
}
