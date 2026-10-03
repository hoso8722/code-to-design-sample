// 実行: java --enable-preview FirstSuccess.java
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

public class FirstSuccess {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %s%n", nowMs(), message);
    }

    // 同じ問い合わせを複数の相手に送り、応答時間が違う、という想定の子タスク（待ちは sleep で模擬する）
    static String replica(String name, long ms) throws InterruptedException {
        log(name + ": 開始（" + (Thread.currentThread().isVirtual() ? "仮想スレッド" : "プラットフォームスレッド") + "）");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log(name + ": 割り込まれて止まった");
            throw e;
        }
        log(name + ": 応答した");
        return name;
    }

    public static void main(String[] args) throws Exception {
        // 最初に成功した子タスクの結果を返し、残りを取り消す
        try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {
            scope.fork(() -> replica("A(300ms)", 300));
            scope.fork(() -> replica("B(100ms)", 100));
            scope.fork(() -> replica("C(200ms)", 200));
            String first = scope.join();  // Joiner によって、join() の戻り値が変わる
            log("main: 結果 = " + first);
        }
    }
}
