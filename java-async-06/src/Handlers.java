// 実行: java Handlers.java
import java.util.concurrent.CompletableFuture;

public class Handlers {
    static CompletableFuture<String> source(boolean fail) {
        return CompletableFuture.supplyAsync(() -> {
            if (fail) throw new IllegalStateException("失敗");
            return "成功";
        });
    }

    static String outcome(CompletableFuture<String> f) {
        try {
            return "値 = " + f.join();
        } catch (Exception e) {
            return "例外 = " + e.getCause().getClass().getSimpleName();
        }
    }

    public static void main(String[] args) {
        for (boolean fail : new boolean[] {false, true}) {
            System.out.println("== 元の処理が" + (fail ? "失敗" : "成功") + "した場合");

            System.out.println("exceptionally : " + outcome(source(fail).exceptionally(e -> "代わりの値")));

            System.out.println("handle        : " + outcome(source(fail).handle((value, e) ->
                    e == null ? value + "（handle で加工）" : "代わりの値（handle）")));

            System.out.println("whenComplete  : " + outcome(source(fail).whenComplete((value, e) ->
                    System.out.println("    whenComplete が見た: value=" + value + ", e=" + e))));
        }
    }
}
