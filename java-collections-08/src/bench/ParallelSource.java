// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.openjdk.jmh.annotations.*;

// 10万個の要素を処理して合計する。データの持ち方と処理の重さによって、並列の効果がどう変わるか
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class ParallelSource {
    static final int N = 100_000;

    @Param({"ArrayList", "LinkedList", "Stream.iterate"})
    String source;

    @Param({"light", "heavy"})
    String work;

    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();

    @Setup
    public void setup() {
        for (int i = 0; i < N; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
    }

    // 1要素あたりの処理。light は掛け算1回、heavy は計算を100回くり返す
    static long process(int x, boolean heavy) {
        if (!heavy) return (long) x * x;
        long v = x;
        for (int i = 0; i < 100; i++) v = (v * 31 + i) % 1_000_003;
        return v;
    }

    Stream<Integer> stream() {
        return switch (source) {
            case "ArrayList" -> arrayList.stream();
            case "LinkedList" -> linkedList.stream();
            default -> Stream.iterate(0, i -> i + 1).limit(N);
        };
    }

    @Benchmark
    public long sequential() {
        boolean heavy = work.equals("heavy");
        return stream().mapToLong(x -> process(x, heavy)).sum();
    }

    @Benchmark
    public long parallel() {
        boolean heavy = work.equals("heavy");
        return stream().parallel().mapToLong(x -> process(x, heavy)).sum();
    }
}
