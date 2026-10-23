// 実行: taskset -c 0 java OneCpu.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class OneCpu {
    public static void main(String[] args) {
        System.out.println("CPU: " + Runtime.getRuntime().availableProcessors() + "、commonPool の並列度: " + ForkJoinPool.commonPool().getParallelism());
        for (int i = 0; i < 3; i++) {
            System.out.println("  " + CompletableFuture.supplyAsync(() -> Thread.currentThread().getName()).join());
        }
    }
}
