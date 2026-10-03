// 実行: java BufferSize.java
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class BufferSize {
    static final int SIZE = 32 * 1024 * 1024;  // 32MB

    // size バイトの配列で、ファイルを最後まで読む。read を呼んだ回数を返す
    static long readWith(Path file, int size) throws IOException {
        long calls = 0;
        byte[] buffer = new byte[size];
        try (InputStream in = new FileInputStream(file.toFile())) {
            while (in.read(buffer) != -1) {
                calls++;
            }
        }
        return calls;
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("buffer", ".bin");
        try {
            byte[] data = new byte[SIZE];
            new Random(1).nextBytes(data);
            Files.write(file, data);

            System.out.println("配列の大きさ,readの回数,時間(ms)");
            for (int size : new int[] {16, 64, 256, 1024, 8192, 65536, 1048576}) {
                long calls = readWith(file, size);  // 計らずに1回実行する
                double[] times = new double[5];
                for (int i = 0; i < times.length; i++) {
                    long start = System.nanoTime();
                    readWith(file, size);
                    times[i] = (System.nanoTime() - start) / 1_000_000.0;
                }
                Arrays.sort(times);
                System.out.printf("%d,%d,%.1f%n", size, calls, times[times.length / 2]);
            }
        } finally {
            Files.delete(file);
        }
    }
}
