// 実行: java Propagation.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class Propagation {
    static int step(int n, int value) {
        System.out.println("  段" + n + " を実行した（入力 " + value + "）");
        if (n == 2) throw new IllegalStateException("段2で失敗");
        return value + 1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== 段2で例外が起きるとき");
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> step(1, 0))
                .thenApply(v -> step(2, v))
                .thenApply(v -> step(3, v))
                .thenApply(v -> step(4, v));

        try {
            f.join();
        } catch (CompletionException e) {
            System.out.println("join() が投げた例外: " + e.getClass().getSimpleName() + "、原因: " + e.getCause());
        }
        try {
            f.get();
        } catch (ExecutionException e) {
            System.out.println("get() が投げた例外: " + e.getClass().getSimpleName() + "、原因: " + e.getCause());
        }

        System.out.println("== 段3の前に exceptionally を入れる");
        int result = CompletableFuture.supplyAsync(() -> step(1, 0))
                .thenApply(v -> step(2, v))
                .exceptionally(e -> {
                    System.out.println("  exceptionally が受け取った例外: " + e);
                    return -1;  // 代わりの値
                })
                .thenApply(v -> step(3, v))
                .join();
        System.out.println("結果: " + result);
    }
}
