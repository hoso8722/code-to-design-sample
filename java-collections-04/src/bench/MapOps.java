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
public class MapOps {
    @Param({"1000", "100000"})
    int n;

    @Param({"HashMap", "LinkedHashMap", "TreeMap"})
    String type;

    Map<Integer, Integer> map;
    Integer[] keys;  // 探すキー（入れた順とは別の、ばらばらの順）
    int i;

    @Setup
    public void setup() {
        map = switch (type) {
            case "HashMap" -> new HashMap<>();
            case "LinkedHashMap" -> new LinkedHashMap<>();
            default -> new TreeMap<>();
        };
        List<Integer> shuffled = new ArrayList<>();
        for (int k = 0; k < n; k++) shuffled.add(k);
        Collections.shuffle(shuffled, new Random(42));
        for (Integer k : shuffled) map.put(k, k);        // ばらばらの順に入れる
        Collections.shuffle(shuffled, new Random(7));
        keys = shuffled.toArray(new Integer[0]);
    }

    @Benchmark
    public Integer get() {                              // ある既存のキーで取り出す
        i = (i + 1) % n;
        return map.get(keys[i]);
    }

    @Benchmark
    public Integer put() {                              // 既存のキーの値を置き換える
        i = (i + 1) % n;
        return map.put(keys[i], i);
    }
}
