// 実行: java ForceCost.java
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ForceCost {
    static final int RECORDS = 2_000;

    // RECORDS 件の行を書く。forceEvery 件ごとに force を呼ぶ（0 なら呼ばない）。かかった時間（ミリ秒）を返す
    static double write(Path file, int forceEvery) throws IOException {
        long start = System.nanoTime();
        try (FileChannel channel = FileChannel.open(file,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 1; i <= RECORDS; i++) {
                byte[] line = String.format("受験者%07d,70,65,80,合格%n", i).getBytes(StandardCharsets.UTF_8);
                channel.write(ByteBuffer.wrap(line));
                if (forceEvery > 0 && i % forceEvery == 0) {
                    channel.force(false);  // このファイルの内容が、ディスクに書かれるまで待つ
                }
            }
        }
        return (System.nanoTime() - start) / 1e6;
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("force", ".csv");
        try {
            System.out.println("forceの間隔,forceの回数,時間(ms),1件あたり(マイクロ秒)");
            for (int forceEvery : new int[] {0, 1000, 100, 10, 1}) {
                write(file, forceEvery);  // 計らずに1回実行する
                double[] times = new double[5];
                for (int i = 0; i < times.length; i++) {
                    times[i] = write(file, forceEvery);
                }
                Arrays.sort(times);
                double median = times[times.length / 2];
                int forces = forceEvery == 0 ? 0 : RECORDS / forceEvery;
                System.out.printf("%s,%d,%.1f,%.1f%n",
                    forceEvery == 0 ? "呼ばない" : forceEvery + "件ごと", forces, median, median * 1000 / RECORDS);
            }
        } finally {
            Files.delete(file);
        }
    }
}
