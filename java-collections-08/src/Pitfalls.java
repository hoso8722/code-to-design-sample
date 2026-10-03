// 実行: java Pitfalls.java
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Pitfalls {
    public static void main(String[] args) {
        // 1. 並列ストリームの中で、共有の ArrayList に追加する（間違い）
        for (int round = 1; round <= 3; round++) {
            List<Integer> shared = new ArrayList<>();
            String result;
            try {
                IntStream.range(0, 100_000).parallel().forEach(shared::add);
                result = "size=" + shared.size();
            } catch (Exception e) {
                result = e.getClass().getSimpleName();
            }
            System.out.println(round + "回目: 共有の ArrayList に add → " + result + "（正しくは 100000）");
        }

        // 2. collect で集める（正しい）
        List<Integer> collected = IntStream.range(0, 100_000).parallel().boxed().collect(Collectors.toList());
        System.out.println("collect で集める → size=" + collected.size() + "、先頭10個 " + collected.subList(0, 10));

        // 3. forEach と forEachOrdered の順序
        StringBuilder a = new StringBuilder(), b = new StringBuilder();
        IntStream.range(0, 12).parallel().forEach(i -> { synchronized (a) { a.append(i).append(' '); } });
        IntStream.range(0, 12).parallel().forEachOrdered(i -> b.append(i).append(' '));
        System.out.println("forEach        : " + a);
        System.out.println("forEachOrdered : " + b);

        // 4. どのスレッドで動いたか
        Set<String> threads = Collections.synchronizedSet(new TreeSet<>());
        IntStream.range(0, 100_000).parallel().forEach(i -> threads.add(Thread.currentThread().getName()));
        System.out.println("動いたスレッド（" + threads.size() + "本）: " + threads);
    }
}
