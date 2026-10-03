// 実行: java CommonPoolJam.java 30
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class CommonPoolJam {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    public static void main(String[] args) {
        int blocking = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        System.out.println("commonPool の並列度: " + ForkJoinPool.getCommonPoolParallelism());
        System.out.println("kind,thread,start_ms,end_ms");

        // 1秒待つ処理（外部APIの呼び出しを sleep で模擬する）を、commonPool に投入する
        List<CompletableFuture<String>> slow = new ArrayList<>();
        for (int i = 0; i < blocking; i++) {
            slow.add(CompletableFuture.supplyAsync(() -> {
                long start = nowMs();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "slow," + Thread.currentThread().getName() + "," + start + "," + nowMs();
            }));
        }

        // そのあとで、すぐ終わるはずの処理を1つ投入する
        long submitted = nowMs();
        String fast = CompletableFuture.supplyAsync(() -> "fast," + Thread.currentThread().getName() + "," + nowMs() + "," + nowMs()).join();
        System.out.println(fast);
        System.out.println("fast を投入してから結果が出るまで: " + (nowMs() - submitted) + " ms");
        slow.forEach(f -> System.out.println(f.join()));
    }
}
