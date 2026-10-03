// 実行: java Mojibake.java
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Mojibake {
    public static void main(String[] args) throws IOException {
        Charset ms932 = Charset.forName("windows-31j");
        String text = "Aさん,合格";
        Path file = Files.createTempFile("scores", ".csv");
        try {
            // UTF-8 で書いて、MS932 として読む
            Files.write(file, text.getBytes(StandardCharsets.UTF_8));
            System.out.println("UTF-8で書き、MS932で読む: " + new String(Files.readAllBytes(file), ms932));

            // MS932 で書いて、UTF-8 として読む
            Files.write(file, text.getBytes(ms932));
            System.out.println("MS932で書き、UTF-8で読む: " + new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

            // 書いたときと同じ文字コードで読む
            System.out.println("MS932で書き、MS932で読む: " + new String(Files.readAllBytes(file), ms932));
        } finally {
            Files.delete(file);
        }
    }
}
