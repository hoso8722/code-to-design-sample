// 実行: java ThreadCost.java
public class ThreadCost {
    static volatile long sink;

    // 何もしないスレッドを count 本、1本ずつ作って start() し、join() で終わりを待つ
    static long startAndJoin(int count) throws InterruptedException {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            Thread t = new Thread(() -> sink++);
            t.start();
            t.join();
        }
        return System.nanoTime() - start;
    }

    // 同じ処理を、スレッドを作らずに main で count 回呼ぶ
    static long directCall(int count) {
        Runnable r = () -> sink++;
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) r.run();
        return System.nanoTime() - start;
    }

    public static void main(String[] args) throws Exception {
        int count = 10_000;
        startAndJoin(count);  // 準備運動
        directCall(count);
        long threadNs = startAndJoin(count);
        long directNs = directCall(count);
        System.out.printf("スレッドを作って実行: 合計 %d ms、1本あたり %.1f マイクロ秒%n",
                threadNs / 1_000_000, threadNs / 1_000.0 / count);
        System.out.printf("メソッドとして実行  : 合計 %.3f ms、1回あたり %.3f マイクロ秒%n",
                directNs / 1e6, directNs / 1_000.0 / count);
    }
}
