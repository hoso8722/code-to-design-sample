// 実行: java Starvation.java
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Starvation {
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2, Thread.ofPlatform().name("pool-", 1).daemon(true).factory());

        // 親タスクが、同じプールに子タスクを投入して、その結果を待つ
        List<Future<String>> parents = List.of("parent-1", "parent-2").stream()
                .map(name -> pool.submit(() -> {
                    Future<String> child = pool.submit(() -> name + " の子タスクの結果");
                    return child.get();  // 子タスクが終わるまで待つ
                }))
                .toList();

        for (Future<String> parent : parents) {
            try {
                System.out.println(parent.get(2, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                System.out.println("2秒待っても親タスクが終わらない");
            }
        }
        System.out.println("main: pid=" + ProcessHandle.current().pid() + "（20秒のうちに jcmd で調べる）");
        Thread.sleep(20_000);
    }
}
