// 実行: java LostUpdate.java
import java.util.ArrayList;
import java.util.List;

public class LostUpdate {
    static long count = 0;  // 8本のスレッドから同時に増やすカウンタ

    public static void main(String[] args) throws Exception {
        int threads = 8, perThread = 1_000_000;
        for (int round = 1; round <= 3; round++) {
            count = 0;
            List<Thread> list = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                Thread thread = new Thread(() -> {
                    for (int i = 0; i < perThread; i++) count++;
                });
                thread.start();
                list.add(thread);
            }
            for (Thread thread : list) thread.join();
            long expected = (long) threads * perThread;
            System.out.printf("%d回目: 期待 %,d  結果 %,d  （%,d 回ぶん消えた）%n", round, expected, count, expected - count);
        }
    }
}
