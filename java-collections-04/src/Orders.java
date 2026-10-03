// 実行: java Orders.java
import java.util.*;

public class Orders {
    public static void main(String[] args) {
        // 1. 同じ順に put したときの、反復の順序
        List<String> input = List.of("banana", "apple", "cherry", "date", "elderberry");
        for (Map<String, Integer> map : List.<Map<String, Integer>>of(new HashMap<>(), new LinkedHashMap<>(), new TreeMap<>())) {
            for (String s : input) map.put(s, s.length());
            System.out.printf("%-13s %s%n", map.getClass().getSimpleName(), map.keySet());
        }

        // 2. TreeMap の順序を使う操作
        TreeMap<Integer, String> scores = new TreeMap<>(Map.of(55, "D", 68, "C", 74, "B", 88, "A", 93, "S"));
        System.out.println("70点以上の最低の点数: " + scores.ceilingKey(70));
        System.out.println("70点未満の最高の点数: " + scores.lowerKey(70));
        System.out.println("60点以上90点未満: " + scores.subMap(60, 90));
        System.out.println("最高点: " + scores.lastEntry());

        // 3. LinkedHashMap で LRU キャッシュ（最大3件。いちばん長く使われていないものを捨てる）
        Map<String, String> cache = new LinkedHashMap<>(16, 0.75f, true) {  // true：使った順に並べる
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > 3;
            }
        };
        cache.put("a", "A");
        cache.put("b", "B");
        cache.put("c", "C");
        System.out.println("a・b・c を入れた: " + cache.keySet());
        cache.get("a");
        System.out.println("a を使った:        " + cache.keySet());
        cache.put("d", "D");
        System.out.println("d を入れた:        " + cache.keySet() + "（b が捨てられた）");
    }
}
