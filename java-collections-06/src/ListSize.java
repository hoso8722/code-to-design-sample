// 実行: java -Xmx2g ListSize.java
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// 要素数 k のリストを大量に作って持ち続け、リスト1つあたりが保持するバイト数を求める（要素そのものは除く）
public class ListSize {
    static long usedAfterGc() {
        for (int i = 0; i < 3; i++) System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    public static void main(String[] args) {
        Map<String, Function<List<String>, List<String>>> ways = new LinkedHashMap<>();
        ways.put("new ArrayList<>(src)", ArrayList::new);
        ways.put("List.copyOf(src)", List::copyOf);
        ways.put("unmodifiableList(src)", src -> Collections.unmodifiableList(new ArrayList<>(src)));
        ways.put("stream().toList()", src -> src.stream().toList());
        ways.put("collect(toList())", src -> src.stream().collect(Collectors.toList()));

        String[] shared = new String[1000];
        for (int i = 0; i < shared.length; i++) shared[i] = "e" + i;  // 要素は全リストで共有する

        System.out.println("way,size,bytes_per_list");
        for (int k : new int[] {0, 1, 2, 10, 1000}) {
            List<String> src = new ArrayList<>(Arrays.asList(shared).subList(0, k));
            int count = k >= 1000 ? 2_000 : 100_000;
            for (var way : ways.entrySet()) {
                Object[] keep = new Object[count];
                long before = usedAfterGc();
                for (int i = 0; i < count; i++) keep[i] = way.getValue().apply(src);
                long after = usedAfterGc();
                System.out.printf("%s,%d,%.1f%n", way.getKey(), k, (double) (after - before) / count);
                java.lang.ref.Reference.reachabilityFence(keep);
            }
        }
    }
}
