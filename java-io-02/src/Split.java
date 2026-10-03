// 実行: java Split.java
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Split {
    public static void main(String[] args) throws IOException {
        byte[] data = "合格発表".getBytes(StandardCharsets.UTF_8);  // 1文字3バイトで、12バイト
        System.out.println("バイト数: " + data.length);

        // 誤り：4バイトずつ読んで、読んだ分だけを文字列に変換する
        StringBuilder wrong = new StringBuilder();
        try (InputStream in = new ByteArrayInputStream(data)) {
            byte[] buffer = new byte[4];
            int n;
            while ((n = in.read(buffer)) != -1) {
                wrong.append(new String(buffer, 0, n, StandardCharsets.UTF_8));
            }
        }
        System.out.println("4バイトずつ変換: " + wrong);

        // 正しい：Reader に変換を任せて、文字の単位で読む
        StringBuilder right = new StringBuilder();
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
            char[] buffer = new char[4];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                right.append(buffer, 0, n);
            }
        }
        System.out.println("Readerで読む: " + right);
    }
}
