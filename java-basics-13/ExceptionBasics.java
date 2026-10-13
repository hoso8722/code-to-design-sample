// 実行: java ExceptionBasics.java
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class ExceptionBasics {
    static int divide(int a, int b) {
        return a / b;
    }

    static int parseScore(String text) {
        return Integer.parseInt(text);
    }

    public static void main(String[] args) throws Exception {
        // try-catch: 例外を捕まえて、続きを実行する
        try {
            System.out.println(divide(10, 0));
        } catch (ArithmeticException e) {
            System.out.println("0で割った: " + e.getMessage());
        }

        // 複数の catch。上から順に調べられる
        try {
            System.out.println(parseScore("abc"));
        } catch (NumberFormatException e) {
            System.out.println("数値ではない: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("その他の実行時の例外: " + e.getMessage());
        }

        // finally: 例外があってもなくても実行される
        try {
            System.out.println("読み込み: " + Files.readString(Path.of("存在しないファイル.txt")));
        } catch (NoSuchFileException e) {
            System.out.println("ファイルがない: " + e.getMessage());
        } finally {
            System.out.println("finally は必ず実行される");
        }

        System.out.println("例外を処理したので、ここまで進める");
    }
}
