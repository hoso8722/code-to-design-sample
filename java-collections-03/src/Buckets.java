// 実行: java --add-opens java.base/java.util=ALL-UNNAMED Buckets.java
import java.lang.reflect.Field;
import java.util.HashMap;

public class Buckets {
    // すべて同じハッシュ値を返すキー（わざと悪い hashCode）
    record BadKey(int id) implements Comparable<BadKey> {
        @Override public int hashCode() { return 42; }
        @Override public int compareTo(BadKey o) { return Integer.compare(id, o.id); }
    }

    public static void main(String[] args) throws Exception {
        Field tableField = HashMap.class.getDeclaredField("table");  // 内部の配列（非公開のフィールド）
        tableField.setAccessible(true);                               // 実行時に --add-opens が必要

        HashMap<BadKey, Integer> map = new HashMap<>();
        System.out.println("要素数,配列の大きさ,キーが入っているバケットの種類");
        for (int i = 1; i <= 12; i++) {
            map.put(new BadKey(i), i);
            Object[] table = (Object[]) tableField.get(map);
            Object bucket = table[42 & (table.length - 1)];  // ハッシュ値 42 が入るバケット
            System.out.printf("%d,%d,%s%n", i, table.length, bucket.getClass().getSimpleName());
        }
    }
}
