// 実行: java ReadWays.java
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class ReadWays {
    static final int SIZE = 4 * 1024 * 1024;  // 4MB

    interface Reader {
        long read(Path file) throws IOException;  // 読んだバイトの合計（確認用）を返す
    }

    // 1. FileInputStream から、1バイトずつ読む
    static long byteByByte(Path file) throws IOException {
        long sum = 0;
        try (InputStream in = new FileInputStream(file.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                sum += b;
            }
        }
        return sum;
    }

    // 2. BufferedInputStream で包んで、1バイトずつ読む
    static long bufferedByteByByte(Path file) throws IOException {
        long sum = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {
            int b;
            while ((b = in.read()) != -1) {
                sum += b;
            }
        }
        return sum;
    }

    // 3. FileInputStream から、8,192バイトの配列にまとめて読む
    static long byArray(Path file) throws IOException {
        long sum = 0;
        byte[] buffer = new byte[8192];
        try (InputStream in = new FileInputStream(file.toFile())) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                for (int i = 0; i < n; i++) {
                    sum += buffer[i] & 0xff;
                }
            }
        }
        return sum;
    }

    // 4. Files.readAllBytes で、全体を1回で読む
    static long allAtOnce(Path file) throws IOException {
        long sum = 0;
        for (byte b : Files.readAllBytes(file)) {
            sum += b & 0xff;
        }
        return sum;
    }

    // 5回計って、中央値（ミリ秒）を返す。その前に1回、計らずに実行する
    static double medianMillis(Reader reader, Path file) throws IOException {
        reader.read(file);
        double[] times = new double[5];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            reader.read(file);
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
        }
        Arrays.sort(times);
        return times[times.length / 2];
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("read", ".bin");
        try {
            byte[] data = new byte[SIZE];
            new Random(1).nextBytes(data);
            Files.write(file, data);

            System.out.printf("1バイトずつ（FileInputStream）       %8.1f ms%n", medianMillis(ReadWays::byteByByte, file));
            System.out.printf("1バイトずつ（BufferedInputStream）   %8.1f ms%n", medianMillis(ReadWays::bufferedByteByByte, file));
            System.out.printf("8,192バイトの配列で読む              %8.1f ms%n", medianMillis(ReadWays::byArray, file));
            System.out.printf("Files.readAllBytes                   %8.1f ms%n", medianMillis(ReadWays::allAtOnce, file));
        } finally {
            Files.delete(file);
        }
    }
}
