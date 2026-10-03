// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// 1万件が入った「待ち行列」に、末尾から1件入れて、先頭から1件取り出す
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class QueueOps {
    List<Integer> arrayList = new ArrayList<>();
    Deque<Integer> linkedList = new LinkedList<>();
    Deque<Integer> arrayDeque = new ArrayDeque<>();

    @Setup
    public void setup() {
        for (int i = 0; i < 10_000; i++) {
            arrayList.add(i);
            linkedList.addLast(i);
            arrayDeque.addLast(i);
        }
    }

    @Benchmark
    public Integer arrayList() {
        arrayList.add(42);
        return arrayList.remove(0);   // 先頭を取り除くと、残りをすべて前にずらす
    }

    @Benchmark
    public Integer linkedList() {
        linkedList.addLast(42);
        return linkedList.pollFirst();
    }

    @Benchmark
    public Integer arrayDeque() {
        arrayDeque.addLast(42);
        return arrayDeque.pollFirst();
    }
}
