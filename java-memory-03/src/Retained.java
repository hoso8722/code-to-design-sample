// 実行: java Retained.java
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Supplier;

// 100万要素のデータ構造を作り、GC後のヒープ使用量の増分（= 保持しているバイト数）を測る
public class Retained {
    static final int N = 1_000_000;

    static long usedAfterGc() {
        for (int i = 0; i < 3; i++) System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    static void measure(String name, Supplier<Object> build) {
        long before = usedAfterGc();
        Object data = build.get();
        long after = usedAfterGc();
        System.out.printf("%-26s %6.1f MB  %5.1f bytes/要素%n",
                name, (after - before) / 1e6, (double) (after - before) / N);
        java.lang.ref.Reference.reachabilityFence(data);  // 計測が終わるまで data を生かしておく
    }

    public static void main(String[] args) {
        measure("int[]", () -> {
            int[] a = new int[N];
            for (int i = 0; i < N; i++) a[i] = i;
            return a;
        });
        measure("Integer[]", () -> {
            Integer[] a = new Integer[N];
            for (int i = 0; i < N; i++) a[i] = i;
            return a;
        });
        measure("ArrayList<Integer>", () -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < N; i++) list.add(i);
            return list;
        });
        measure("LinkedList<Integer>", () -> {
            List<Integer> list = new LinkedList<>();
            for (int i = 0; i < N; i++) list.add(i);
            return list;
        });
        measure("HashSet<Integer>", () -> {
            Set<Integer> set = new HashSet<>();
            for (int i = 0; i < N; i++) set.add(i);
            return set;
        });
        measure("HashMap<Integer,Integer>", () -> {
            Map<Integer, Integer> map = new HashMap<>();
            for (int i = 0; i < N; i++) map.put(i, i);
            return map;
        });
    }
}
