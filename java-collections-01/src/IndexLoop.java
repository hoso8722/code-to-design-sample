// 実行: java IndexLoop.java
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class IndexLoop {
    static long byIndex(List<Integer> list) {     // 添字で1つずつ取り出す
        long sum = 0;
        for (int i = 0; i < list.size(); i++) sum += list.get(i);
        return sum;
    }

    static long byIterator(List<Integer> list) {  // 拡張 for 文（イテレータ）で取り出す
        long sum = 0;
        for (int v : list) sum += v;
        return sum;
    }

    public static void main(String[] args) {
        for (List<Integer> list : List.<List<Integer>>of(new ArrayList<>(), new LinkedList<>())) {
            for (int i = 0; i < 100_000; i++) list.add(i);
            long t0 = System.nanoTime();
            long a = byIndex(list);
            long t1 = System.nanoTime();
            long b = byIterator(list);
            long t2 = System.nanoTime();
            System.out.printf("%-10s 添字で %,6d ms、拡張 for 文で %,4d ms（合計 %d, %d）%n",
                    list.getClass().getSimpleName(), (t1 - t0) / 1_000_000, (t2 - t1) / 1_000_000, a, b);
        }
    }
}
