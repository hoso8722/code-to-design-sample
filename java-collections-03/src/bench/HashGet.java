// JMH のベンチマーク（npm run extract-code では実行できない。記事の「計測する」の手順で実行する）
package bench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

// n 個のキーが入った HashMap から、1つのキーで get する時間
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
@State(Scope.Thread)
public class HashGet {
    // 1. ふつうのキー（record が id から hashCode を作る）
    record GoodKey(int id) {}

    // 2. すべて同じハッシュ値を返すが、大小を比べられるキー
    record BadComparableKey(int id) implements Comparable<BadComparableKey> {
        @Override public int hashCode() { return 42; }
        @Override public int compareTo(BadComparableKey o) { return Integer.compare(id, o.id); }
    }

    // 3. すべて同じハッシュ値を返し、大小も比べられないキー
    record BadPlainKey(int id) {
        @Override public int hashCode() { return 42; }
    }

    @Param({"100", "1000", "10000"})
    int n;

    @Param({"good", "badComparable", "badPlain"})
    String keyType;

    Map<Object, Integer> map = new HashMap<>();
    Object target;

    Object key(int id) {
        return switch (keyType) {
            case "good" -> new GoodKey(id);
            case "badComparable" -> new BadComparableKey(id);
            default -> new BadPlainKey(id);
        };
    }

    @Setup
    public void setup() {
        for (int i = 0; i < n; i++) map.put(key(i), i);
        target = key(n / 2);  // 真ん中あたりのキー（新しく作った、等しいオブジェクト）
    }

    @Benchmark
    public Integer get() {
        return map.get(target);
    }
}
