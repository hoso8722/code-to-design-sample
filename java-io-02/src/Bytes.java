// 実行: java Bytes.java
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Bytes {
    // バイトの配列を、16進数で表示する
    static String hex(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        for (byte b : bytes) {
            text.append(String.format("%02X ", b));
        }
        return text.toString().trim();
    }

    public static void main(String[] args) {
        List<Charset> charsets = List.of(
            StandardCharsets.UTF_8,
            Charset.forName("windows-31j"),  // Windows で使われてきた Shift_JIS の一種（MS932）
            Charset.forName("EUC-JP"),
            StandardCharsets.UTF_16BE);

        for (String text : List.of("A", "あ", "合格")) {
            System.out.println("「" + text + "」");
            for (Charset charset : charsets) {
                byte[] bytes = text.getBytes(charset);
                System.out.printf("  %-11s %dバイト: %s%n", charset.name(), bytes.length, hex(bytes));
            }
        }

        String line = "Aさん,数学80点,合格";
        System.out.println("「" + line + "」（" + line.length() + "文字）");
        for (Charset charset : charsets) {
            System.out.printf("  %-11s %dバイト%n", charset.name(), line.getBytes(charset).length);
        }
    }
}
