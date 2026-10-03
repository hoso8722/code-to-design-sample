// 実行: java Newline.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Newline {
    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("result", ".txt");
        try {
            // Windows の改行（CR LF）で書かれたファイル
            Files.writeString(file, "合格\r\n不合格\r\n");

            // 誤り：全体を読んで、"\n" で分割する
            String[] parts = Files.readString(file).split("\n");
            System.out.println("split: 1行目の文字数 " + parts[0].length()
                + "、\"合格\" と等しいか " + parts[0].equals("合格"));

            // 正しい：行として読む
            List<String> lines = Files.readAllLines(file);
            System.out.println("readAllLines: 1行目の文字数 " + lines.get(0).length()
                + "、\"合格\" と等しいか " + lines.get(0).equals("合格"));

            System.out.println("この環境の改行: " + System.lineSeparator().replace("\r", "CR").replace("\n", "LF"));
        } finally {
            Files.delete(file);
        }
    }
}
