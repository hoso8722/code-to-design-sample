// 実行: java Copy.java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;

public class Copy {
    interface Copier {
        void copy(Path source, Path target) throws IOException;
    }

    // 1. ストリームで、8,192バイトずつ読んで書く
    static void streamLoop(Path source, Path target) throws IOException {
        try (InputStream in = new FileInputStream(source.toFile());
             OutputStream out = new FileOutputStream(target.toFile())) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        }
    }

    // 2. Files.copy に任せる
    static void filesCopy(Path source, Path target) throws IOException {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    // 3. FileChannel の transferTo で、チャネルからチャネルへ転送する
    static void transferTo(Path source, Path target) throws IOException {
        try (FileChannel in = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel out = FileChannel.open(target, StandardOpenOption.CREATE,
                 StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            long position = 0;
            long size = in.size();
            while (position < size) {
                position += in.transferTo(position, size - position, out);
            }
        }
    }

    // 4. コピー元をメモリマップして、その内容を書く
    static void mapped(Path source, Path target) throws IOException {
        try (FileChannel in = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel out = FileChannel.open(target, StandardOpenOption.CREATE,
                 StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            MappedByteBuffer buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
            while (buffer.hasRemaining()) {
                out.write(buffer);
            }
        }
    }

    static double medianMillis(Copier copier, Path source, Path target) throws IOException {
        copier.copy(source, target);  // 計らずに1回実行する
        double[] times = new double[5];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            copier.copy(source, target);
            times[i] = (System.nanoTime() - start) / 1e6;
        }
        Arrays.sort(times);
        return times[times.length / 2];
    }

    public static void main(String[] args) throws IOException {
        Path source = Files.createTempFile("source", ".bin");
        Path target = Files.createTempFile("target", ".bin");
        try {
            System.out.println("大きさ(MB),ストリーム,Files.copy,transferTo,メモリマップ");
            for (int megabytes : new int[] {16, 64, 256}) {
                byte[] chunk = new byte[1024 * 1024];
                Random random = new Random(1);
                try (OutputStream out = Files.newOutputStream(source)) {
                    for (int i = 0; i < megabytes; i++) {
                        random.nextBytes(chunk);
                        out.write(chunk);
                    }
                }
                System.out.printf("%d,%.1f,%.1f,%.1f,%.1f%n", megabytes,
                    medianMillis(Copy::streamLoop, source, target),
                    medianMillis(Copy::filesCopy, source, target),
                    medianMillis(Copy::transferTo, source, target),
                    medianMillis(Copy::mapped, source, target));
                if (Files.mismatch(source, target) != -1) {
                    throw new IllegalStateException("コピーの内容が一致しません");
                }
            }
        } finally {
            Files.delete(source);
            Files.delete(target);
        }
    }
}
