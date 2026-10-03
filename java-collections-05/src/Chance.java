// 実行: java Chance.java
import java.util.HashSet;
import java.util.Set;

// hashCode を実装し忘れたクラスで、「等しいオブジェクト」が HashSet の中に見つかる割合
public class Chance {
    static final class EqualsOnly {
        final String name;
        EqualsOnly(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof EqualsOnly other && name.equals(other.name);
        }
    }

    public static void main(String[] args) {
        int trials = 10_000, found = 0;
        for (int i = 0; i < trials; i++) {
            Set<EqualsOnly> set = new HashSet<>();
            set.add(new EqualsOnly("alice"));
            if (set.contains(new EqualsOnly("alice"))) found++;  // 同じ内容の、別のオブジェクトで探す
        }
        System.out.printf("%,d 回のうち、見つかったのは %,d 回（%.2f%%）%n", trials, found, 100.0 * found / trials);
    }
}
