// 実行: java --enable-preview FailFast.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

public class FailFast {
    static final long T0 = System.nanoTime();

    static long nowMs() {
        return (System.nanoTime() - T0) / 1_000_000;
    }

    static void log(String message) {
        System.out.printf("%4d ms  %s%n", nowMs(), message);
    }

    // ms ミリ秒待ってから結果を返す（fail なら例外を投げる）子タスク。待ちは sleep で模擬する
    static String call(String name, long ms, boolean fail) throws InterruptedException {
        log(name + ": 開始");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log(name + ": 割り込まれて止まった");
            throw e;
        }
        if (fail) {
            log(name + ": 失敗した");
            throw new IllegalStateException(name + " で失敗");
        }
        log(name + ": 成功した");
        return name;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("== StructuredTaskScope（プレビュー機能）");
        long start = nowMs();
        try (var scope = StructuredTaskScope.open()) {  // デフォルト：すべて成功するか、1つでも失敗するまで待つ
            Subtask<String> a = scope.fork(() -> call("A(100ms・失敗)", 100, true));
            Subtask<String> b = scope.fork(() -> call("B(300ms)", 300, false));
            Subtask<String> c = scope.fork(() -> call("C(500ms)", 500, false));
            scope.join();
            log("main: すべて成功 " + a.get() + b.get() + c.get());
        } catch (StructuredTaskScope.FailedException e) {
            log("main: join() が FailedException を投げた。原因: " + e.getCause().getMessage());
        }
        log("main: スコープを閉じ終わった（" + (nowMs() - start) + " ms）");

        Thread.sleep(600);  // 前の実験の出力と混ざらないように、少し待つ

        System.out.println("== CompletableFuture.allOf");
        start = nowMs();
        CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> unchecked("A(100ms・失敗)", 100, true));
        CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> unchecked("B(300ms)", 300, false));
        CompletableFuture<String> c = CompletableFuture.supplyAsync(() -> unchecked("C(500ms)", 500, false));
        try {
            CompletableFuture.allOf(a, b, c).join();
        } catch (CompletionException e) {
            log("main: allOf が例外で終わった。原因: " + e.getCause().getMessage());
        }
        log("main: allOf の待ちが終わった（" + (nowMs() - start) + " ms）");
    }

    static String unchecked(String name, long ms, boolean fail) {
        try {
            return call(name, ms, fail);
        } catch (InterruptedException e) {
            throw new CompletionException(e);
        }
    }
}
