// 実行: java GetOrder.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GetOrder {
    static final long T0 = System.nanoTime();
    static final List<String> records = Collections.synchronizedList(new ArrayList<>());

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    // ms ミリ秒待ってから name を返すタスク（待ちは sleep で模擬する）
    static Callable<String> task(String name, long ms) {
        return () -> {
            long start = nowMs();
            Thread.sleep(ms);
            records.add(name + "," + Thread.currentThread().getName() + "," + start + "," + nowMs());
            return name;
        };
    }

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            Future<String> slow = pool.submit(task("slow(300ms)", 300));
            Future<String> fast = pool.submit(task("fast(100ms)", 100));
            Future<String> middle = pool.submit(task("middle(200ms)", 200));

            // 投入した順に get() する。get() は結果が出るまで main を止める
            for (Future<String> f : List.of(slow, fast, middle)) {
                long start = nowMs();
                String result = f.get();
                records.add("get " + result + ",main," + start + "," + nowMs());
            }
        }
        System.out.println("name,thread,start_ms,end_ms");
        records.forEach(System.out::println);
    }
}
