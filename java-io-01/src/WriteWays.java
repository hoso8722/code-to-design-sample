// 実行: java WriteWays.java
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class WriteWays {
    static final int SIZE = 1024 * 1024;  // 1MB

    interface Writer {
        void write(Path file, byte[] data) throws IOException;
    }

    // 1. FileOutputStream に、1バイトずつ書く
    static void byteByByte(Path file, byte[] data) throws IOException {
        try (OutputStream out = new FileOutputStream(file.toFile())) {
            for (byte b : data) {
                out.write(b);
            }
        }
    }

    // 2. BufferedOutputStream で包んで、1バイトずつ書く
    static void bufferedByteByByte(Path file, byte[] data) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file.toFile()))) {
            for (byte b : data) {
                out.write(b);
            }
        }
    }

    // 3. FileOutputStream に、配列ごと1回で書く
    static void allAtOnce(Path file, byte[] data) throws IOException {
        try (OutputStream out = new FileOutputStream(file.toFile())) {
            out.write(data);
        }
    }

    static double medianMillis(Writer writer, Path file, byte[] data) throws IOException {
        writer.write(file, data);
        double[] times = new double[5];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            writer.write(file, data);
            times[i] = (System.nanoTime() - start) / 1_000_000.0;
        }
        Arrays.sort(times);
        return times[times.length / 2];
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("write", ".bin");
        try {
            byte[] data = new byte[SIZE];
            new Random(1).nextBytes(data);

            System.out.printf("1バイトずつ（FileOutputStream）      %8.1f ms%n", medianMillis(WriteWays::byteByByte, file, data));
            System.out.printf("1バイトずつ（BufferedOutputStream）  %8.1f ms%n", medianMillis(WriteWays::bufferedByteByByte, file, data));
            System.out.printf("配列ごと1回で書く                    %8.1f ms%n", medianMillis(WriteWays::allAtOnce, file, data));
        } finally {
            Files.delete(file);
        }
    }
}
