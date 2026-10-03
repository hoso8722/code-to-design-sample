// 実行: java MapCount.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCount {
    // 8本のスレッドで、100個のキーをそれぞれ合計 80,000 回ずつ数える
    static String run(String name, Map<Integer, Integer> map, boolean useMerge) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        for (int t = 0; t < 8; t++) {
            Thread thread = new Thread(() -> {
                for (int i = 0; i < 1_000_000; i++) {
                    int key = i % 100;
                    if (useMerge) {
                        map.merge(key, 1, Integer::sum);         // 読んで足して書く、を1つの操作で
                    } else {
                        Integer old = map.get(key);              // 読む
                        map.put(key, old == null ? 1 : old + 1); // 足して書く（2つの操作）
                    }
                }
            });
            thread.setUncaughtExceptionHandler((th, e) -> {
                synchronized (errors) { errors.add(e); }
            });
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) thread.join();
        long total = map.values().stream().mapToLong(Integer::longValue).sum();
        return String.format("%-36s キーの数 %3d、合計 %,10d（期待 8,000,000）、例外 %d 件%s",
                name, map.size(), total, errors.size(), errors.isEmpty() ? "" : "（" + errors.get(0).getClass().getSimpleName() + "）");
    }

    public static void main(String[] args) throws Exception {
        System.out.println(run("HashMap + get/put", new HashMap<>(), false));
        System.out.println(run("HashMap + merge", new HashMap<>(), true));
        System.out.println(run("ConcurrentHashMap + get/put", new ConcurrentHashMap<>(), false));
        System.out.println(run("ConcurrentHashMap + merge", new ConcurrentHashMap<>(), true));
    }
}
