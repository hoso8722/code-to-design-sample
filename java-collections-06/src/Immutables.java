// 実行: java Immutables.java
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Immutables {
    static String tryRun(Runnable r) {
        try {
            r.run();
            return "できた";
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) {
        Map<String, Function<List<String>, List<String>>> ways = new LinkedHashMap<>();
        ways.put("List.of(...)", src -> List.of(src.toArray(new String[0])));
        ways.put("List.copyOf(src)", List::copyOf);
        ways.put("unmodifiableList(src)", Collections::unmodifiableList);
        ways.put("stream().toList()", src -> src.stream().toList());
        ways.put("collect(toList())", src -> src.stream().collect(Collectors.toList()));

        System.out.printf("%-22s %-10s %-34s %-10s %s%n", "作り方", "add", "元の src に追加したあとの中身", "null を含む", "クラス");
        for (var way : ways.entrySet()) {
            List<String> src = new ArrayList<>(List.of("a", "b"));
            List<String> list = way.getValue().apply(src);

            String add = tryRun(() -> list.add("x"));             // 1. 変更できるか
            src.add("c");                                         // 2. 元のリストを変更すると、見えるか
            String view = list.toString();
            List<String> withNull = new ArrayList<>(Arrays.asList("a", null));
            String nulls = tryRun(() -> way.getValue().apply(withNull));  // 3. null を含むリストから作れるか

            System.out.printf("%-22s %-10s %-34s %-10s %s%n", way.getKey(), add, view, nulls, list.getClass().getName());
        }
    }
}
