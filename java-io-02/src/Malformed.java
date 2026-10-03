// 実行: java Malformed.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Malformed {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("scores", ".csv");
        try {
            // MS932 で書いたファイルを、UTF-8 として読む
            Files.write(file, "Aさん,合格".getBytes(Charset.forName("windows-31j")));

            // 1. new String：不正なバイトを、置換文字（U+FFFD）に置き換える
            String replaced = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            System.out.println("new String: " + replaced + "（置換文字 "
                + replaced.chars().filter(c -> c == 0xFFFD).count() + "個）");

            // 2. InputStreamReader：同じく、置換文字に置き換える
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
                System.out.println("InputStreamReader: " + reader.readLine());
            }

            // 3. Files.readString：例外を投げる
            try {
                System.out.println("Files.readString: " + Files.readString(file));
            } catch (IOException e) {
                System.out.println("Files.readString: " + e);
            }

            // 4. Files.newBufferedReader：例外を投げる
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                System.out.println("Files.newBufferedReader: " + reader.readLine());
            } catch (IOException e) {
                System.out.println("Files.newBufferedReader: " + e);
            }
        } finally {
            Files.delete(file);
        }
    }
}
