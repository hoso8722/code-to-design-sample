// 実行: java ListMapBasics.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ListMapBasics {
    public static void main(String[] args) {
        // List: 順番のある、長さの変えられる入れ物
        List<String> names = new ArrayList<>();
        names.add("田中");
        names.add("鈴木");
        names.add("佐藤");
        names.remove("鈴木");
        names.add(0, "高橋");
        System.out.println("List: " + names + "、件数 " + names.size() + "、先頭 " + names.get(0));
        System.out.println("contains(\"田中\"): " + names.contains("田中"));

        // Map: キーと値の組
        Map<String, Integer> scores = new HashMap<>();
        scores.put("田中", 75);
        scores.put("佐藤", 90);
        scores.put("田中", 80);                 // 同じキーに put すると、値が置き換わる
        System.out.println("田中の点数: " + scores.get("田中"));
        System.out.println("いないキー: " + scores.get("高橋"));
        System.out.println("getOrDefault: " + scores.getOrDefault("高橋", 0));
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            System.out.println("  " + entry.getKey() + " → " + entry.getValue());
        }

        // Set: 重複を許さない入れ物
        Set<String> unique = new HashSet<>(List.of("A", "B", "A", "C", "B"));
        System.out.println("Set の件数: " + unique.size());
    }
}
