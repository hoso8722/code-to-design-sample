// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class StreamVsLoop {
    @Param({"100", "10000", "1000000"})
    int n;

    int[] array;
    List<Integer> list;

    @Setup
    public void setup() {
        array = new int[n];
        list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            array[i] = i % 1000;
            list.add(i % 1000);
        }
    }

    // 1. 合計
    @Benchmark
    public long sumForArray() {                 // int[] を for で
        long sum = 0;
        for (int v : array) sum += v;
        return sum;
    }

    @Benchmark
    public long sumIntStream() {                // int[] を IntStream で
        return Arrays.stream(array).asLongStream().sum();
    }

    @Benchmark
    public long sumBoxedStream() {              // List<Integer> を Stream<Integer> で（ボクシングしたまま）
        return list.stream().reduce(0L, (acc, v) -> acc + v, Long::sum);
    }

    @Benchmark
    public long sumMapToLong() {                // List<Integer> を mapToLong でプリミティブにしてから
        return list.stream().mapToLong(Integer::longValue).sum();
    }

    // 2. 偶数だけを取り出して2乗し、合計する
    @Benchmark
    public long filterMapFor() {
        long sum = 0;
        for (int v : list) {
            if (v % 2 == 0) sum += (long) v * v;
        }
        return sum;
    }

    @Benchmark
    public long filterMapStream() {
        return list.stream().filter(v -> v % 2 == 0).mapToLong(v -> (long) v * v).sum();
    }
}
