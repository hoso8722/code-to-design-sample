// 実行: java FileSpeed.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

// 指定したフォルダ（省略すると今のフォルダ）に、小さなファイルを1,000個書いて読み、1MBのファイルを64個書いて読む時間を計る
public class FileSpeed {
    static double median(double[] times) {
        Arrays.sort(times);
        return times[times.length / 2];
    }

    static void deleteAll(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.toList()) {
                Files.delete(file);
            }
        }
        Files.delete(dir);
    }

    // 小さなファイル（100バイト）を count 個書いて、すべて読む
    static double smallFiles(Path base, int count) throws IOException {
        Path dir = Files.createTempDirectory(base, "small");
        long start = System.nanoTime();
        byte[] data = new byte[100];
        for (int i = 0; i < count; i++) {
            Files.write(dir.resolve("f" + i + ".txt"), data);
        }
        long total = 0;
        for (int i = 0; i < count; i++) {
            total += Files.readAllBytes(dir.resolve("f" + i + ".txt")).length;
        }
        double ms = (System.nanoTime() - start) / 1e6;
        deleteAll(dir);
        if (total != 100L * count) throw new IllegalStateException();
        return ms;
    }

    // 1MBのファイルを count 個書いて、すべて読む
    static double largeFiles(Path base, int count) throws IOException {
        Path dir = Files.createTempDirectory(base, "large");
        long start = System.nanoTime();
        byte[] data = new byte[1024 * 1024];
        for (int i = 0; i < count; i++) {
            Files.write(dir.resolve("f" + i + ".bin"), data);
        }
        for (int i = 0; i < count; i++) {
            Files.readAllBytes(dir.resolve("f" + i + ".bin"));
        }
        double ms = (System.nanoTime() - start) / 1e6;
        deleteAll(dir);
        return ms;
    }

    public static void main(String[] args) throws IOException {
        Path base = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath().normalize();
        smallFiles(base, 1_000);  // 計らずに1回実行する
        double[] small = new double[5];
        double[] large = new double[5];
        for (int i = 0; i < 5; i++) {
            small[i] = smallFiles(base, 1_000);
            large[i] = largeFiles(base, 64);
        }
        System.out.printf("%s: 小さなファイル1,000個 %.0f ms、1MBのファイル64個 %.0f ms%n",
            base, median(small), median(large));
    }
}
