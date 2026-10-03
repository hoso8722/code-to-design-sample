// 実行: java NoShutdown.java close
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoShutdown {
    public static void main(String[] args) throws Exception {
        boolean close = args.length > 0 && args[0].equals("close");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> System.out.println("タスクを実行した: " + Thread.currentThread().getName())).get();
        if (close) {
            pool.close();  // shutdown() して、実行中のタスクが終わるのを待つ（JDK 19以降）
        }
        System.out.println("main: 終わる（close したか? " + close + "）");
    }
}
