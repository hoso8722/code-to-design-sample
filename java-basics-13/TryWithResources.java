// 実行: java TryWithResources.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TryWithResources {
    public static void main(String[] args) throws IOException {
        Path path = Files.createTempFile("scores", ".txt");
        Files.writeString(path, "田中,75\n鈴木,55\n");

        // try-with-resources: 閉じる必要のあるものを、自動で閉じる
        try (var lines = Files.lines(path)) {
            lines.forEach(line -> System.out.println("読んだ行: " + line));
        }

        List<String> all = Files.readAllLines(path);
        System.out.println("行数: " + all.size());
        Files.delete(path);
    }
}
