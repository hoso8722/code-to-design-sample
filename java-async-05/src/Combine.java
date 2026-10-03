// 実行: java Combine.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Combine {
    static final long T0 = System.nanoTime();
    static final List<String> records = Collections.synchronizedList(new ArrayList<>());

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    // name の情報を ms ミリ秒かけて取ってくる（待ちは sleep で模擬する）
    static String fetch(String mode, String name, long ms) {
        long start = nowMs();
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        records.add(mode + "," + name + "," + Thread.currentThread().getName() + "," + start + "," + nowMs());
        return name;
    }

    public static void main(String[] args) {
        // 1. 順番に：ユーザー（200ms）を取ってから、注文（200ms）を取る
        long start = nowMs();
        String user = fetch("sequential", "user", 200);
        String order = fetch("sequential", "order", 200);
        System.out.println("順番に: " + user + "+" + order + " " + (nowMs() - start) + " ms");

        // 2. thenCombine：2つを同時に取りにいき、両方そろったら組み合わせる
        start = nowMs();
        CompletableFuture<String> userF = CompletableFuture.supplyAsync(() -> fetch("combine", "user", 200));
        CompletableFuture<String> orderF = CompletableFuture.supplyAsync(() -> fetch("combine", "order", 200));
        String combined = userF.thenCombine(orderF, (u, o) -> u + "+" + o).join();
        System.out.println("thenCombine: " + combined + " " + (nowMs() - start) + " ms");

        // 3. thenCompose：ユーザーを取ってから、その結果を使って注文を取る（順番が必要な場合）
        start = nowMs();
        String composed = CompletableFuture.supplyAsync(() -> fetch("compose", "user", 200))
                .thenCompose(u -> CompletableFuture.supplyAsync(() -> fetch("compose", "order-of-" + u, 200)))
                .join();
        System.out.println("thenCompose: " + composed + " " + (nowMs() - start) + " ms");

        System.out.println("mode,name,thread,start_ms,end_ms");
        records.forEach(System.out::println);
    }
}
