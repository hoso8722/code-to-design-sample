// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
import java.nio.file.Files;
import java.nio.file.Path;

public class UncheckedException {
    public static void main(String[] args) {
        String text = Files.readString(Path.of("scores.txt"));
        System.out.println(text);
    }
}
