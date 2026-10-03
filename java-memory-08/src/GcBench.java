// 実行: java -Xms1g -Xmx1g -XX:+UseG1GC GcBench.java
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.SplittableRandom;

public class GcBench {
    static long peakRssMB() throws Exception {
        for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
            if (line.startsWith("VmHWM:")) return Long.parseLong(line.replaceAll("[^0-9]", "")) / 1024;  // RSSの最大値
        }
        throw new IllegalStateException("VmHWM が見つかりません");
    }

    public static void main(String[] args) throws Exception {
        int slots = 2_000_000;
        byte[][] live = new byte[slots][];  // 長生きするオブジェクト（最大200万個、約170MB）
        SplittableRandom random = new SplittableRandom(42);

        long start = System.nanoTime();
        long total = 0;
        for (long i = 0; i < 200_000_000L; i++) {
            byte[] data = new byte[64];
            if ((i & 15) == 0) {
                live[random.nextInt(slots)] = data;  // 16個に1個は長生きする（古いものと入れ替わる）
            }
            total += data.length;
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("elapsed_ms=%d peak_rss_mb=%d allocated_gb=%.1f%n", elapsedMs, peakRssMB(), total / 1e9);
    }
}
