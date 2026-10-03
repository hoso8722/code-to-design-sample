// 実行: java -Xmx64m Limit.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Limit {
    public static void main(String[] args) throws IOException {
        System.out.printf("ヒープの上限: %d MB%n", Runtime.getRuntime().maxMemory() / (1024 * 1024));
        Path file = Files.createTempFile("scores", ".csv");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (int i = 1; i <= 2_000_000; i++) {
                    writer.write(String.format("受験者%07d,%d,%d,%d%n", i, i % 101, (i * 7) % 101, (i * 13) % 101));
                }
            }
            System.out.printf("ファイル: %.1f MB%n", Files.size(file) / 1e6);

            // 1行ずつ読む
            long count = 0;
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                while (reader.readLine() != null) {
                    count++;
                }
            }
            System.out.printf("BufferedReader: %,d行を読めました%n", count);

            // 全部読む
            try {
                List<String> lines = Files.readAllLines(file);
                System.out.printf("Files.readAllLines: %,d行を読めました%n", lines.size());
            } catch (OutOfMemoryError e) {
                System.out.println("Files.readAllLines: " + e);
            }
        } finally {
            Files.delete(file);
        }
    }
}
