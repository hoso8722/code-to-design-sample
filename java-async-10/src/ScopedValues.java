// 実行: java --enable-preview ScopedValues.java
import java.util.concurrent.StructuredTaskScope;

public class ScopedValues {
    // リクエストを処理しているユーザー（ScopedValue は JDK 25 で正式機能）
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    static String work(String name) {
        return name + " から見た USER = " + (USER.isBound() ? USER.get() : "（束縛されていない）");
    }

    public static void main(String[] args) throws Exception {
        ScopedValue.where(USER, "alice").call(() -> {
            System.out.println(work("親（where の中）"));
            try (var scope = StructuredTaskScope.open()) {  // 構造化並行性はプレビュー機能
                var a = scope.fork(() -> work("子タスク1"));
                var b = scope.fork(() -> work("子タスク2"));
                scope.join();
                System.out.println(a.get());
                System.out.println(b.get());
            }
            Thread t = Thread.ofPlatform().start(() -> System.out.println(work("ふつうに start したスレッド")));
            t.join();
            return null;
        });
        System.out.println(work("親（where の外）"));
    }
}
