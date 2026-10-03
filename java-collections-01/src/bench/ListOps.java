// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class ListOps {
    @Param({"1000", "10000", "100000"})
    int size;

    @Param({"ArrayList", "LinkedList"})
    String type;

    List<Integer> list;

    @Setup
    public void setup() {
        list = type.equals("ArrayList") ? new ArrayList<>() : new LinkedList<>();
        for (int i = 0; i < size; i++) list.add(i);
    }

    @Benchmark
    public Integer getMiddle() {            // 真ん中の要素を添字で取る
        return list.get(size / 2);
    }

    @Benchmark
    public Integer addLast() {              // 末尾に追加して、末尾から取り除く（要素数を保つ）
        list.add(42);
        return list.remove(list.size() - 1);
    }

    @Benchmark
    public Integer addFirst() {             // 先頭に追加して、先頭から取り除く
        list.add(0, 42);
        return list.remove(0);
    }

    @Benchmark
    public Integer addMiddle() {            // 真ん中に追加して、真ん中から取り除く
        list.add(size / 2, 42);
        return list.remove(size / 2);
    }

    @Benchmark
    public long iterate() {                 // 全体を走査して合計する
        long sum = 0;
        for (int v : list) sum += v;
        return sum;
    }
}
