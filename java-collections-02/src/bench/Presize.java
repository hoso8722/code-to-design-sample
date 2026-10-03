// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// n 個の要素を追加したリストを作る時間（容量を指定しない／指定する）
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class Presize {
    static final Integer VALUE = 1_000;  // 同じオブジェクトを入れて、リスト自体の手間だけを計る

    @Param({"100", "10000", "1000000"})
    int n;

    @Benchmark
    public List<Integer> defaultCapacity() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(VALUE);
        return list;
    }

    @Benchmark
    public List<Integer> presized() {
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(VALUE);
        return list;
    }
}
