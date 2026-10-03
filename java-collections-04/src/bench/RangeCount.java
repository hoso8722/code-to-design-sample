// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// 10万個のキーの中から、「50,000 以上 51,000 未満」のキーの数を数える
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class RangeCount {
    HashMap<Integer, Integer> hashMap = new HashMap<>();
    TreeMap<Integer, Integer> treeMap = new TreeMap<>();

    @Setup
    public void setup() {
        for (int k = 0; k < 100_000; k++) {
            hashMap.put(k, k);
            treeMap.put(k, k);
        }
    }

    @Benchmark
    public int treeMapSubMap() {                  // 範囲の部分だけを取り出して数える
        return treeMap.subMap(50_000, 51_000).size();
    }

    @Benchmark
    public int hashMapScan() {                    // すべてのキーを調べて数える
        int count = 0;
        for (Integer k : hashMap.keySet()) {
            if (k >= 50_000 && k < 51_000) count++;
        }
        return count;
    }
}
