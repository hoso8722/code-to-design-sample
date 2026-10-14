// 検証しない: 複数ファイルの例。collect.sh で java errors/Errors.java として実行している
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// 受け手の1つが例外を投げたとき、残りの受け手に通知が届くか
public class Errors {
    static List<String> run(boolean guarded) {
        List<String> received = new ArrayList<>();
        List<Consumer<String>> listeners = List.of(
            name -> received.add("画面:" + name),
            name -> { throw new IllegalStateException("集計で障害"); },
            name -> received.add("ログ:" + name));
        try {
            for (Consumer<String> l : listeners) {
                if (guarded) {
                    try {
                        l.accept("佐藤");
                    } catch (RuntimeException e) {
                        received.add("（例外を記録: " + e.getMessage() + "）");
                    }
                } else {
                    l.accept("佐藤");
                }
            }
        } catch (RuntimeException e) {
            received.add("（通知が途中で止まった: " + e.getMessage() + "）");
        }
        return received;
    }

    public static void main(String[] args) {
        System.out.println("例外を処理しないループ: " + run(false));
        System.out.println("受け手ごとに例外を処理するループ: " + run(true));
    }
}
