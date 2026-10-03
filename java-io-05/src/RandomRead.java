// 実行: java RandomRead.java
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Random;

public class RandomRead {
    static final int FILE_SIZE = 64 * 1024 * 1024;  // 64MB
    static final int READS = 1_000_000;

    // 1. チャネルの read で、指定した位置から8バイトを読む
    static long byChannel(FileChannel channel, long[] positions) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        long sum = 0;
        for (long position : positions) {
            buffer.clear();
            channel.read(buffer, position);
            sum += buffer.getLong(0);
        }
        return sum;
    }

    // 2. メモリマップしたバッファから、指定した位置の8バイトを読む
    static long byMapped(MappedByteBuffer mapped, long[] positions) {
        long sum = 0;
        for (long position : positions) {
            sum += mapped.getLong((int) position);
        }
        return sum;
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("random", ".bin");
        try {
            byte[] chunk = new byte[1024 * 1024];
            Random random = new Random(1);
            try (OutputStream out = Files.newOutputStream(file)) {
                for (int i = 0; i < FILE_SIZE / chunk.length; i++) {
                    random.nextBytes(chunk);
                    out.write(chunk);
                }
            }
            long[] positions = new long[READS];
            for (int i = 0; i < READS; i++) {
                positions[i] = random.nextInt(FILE_SIZE - 8);
            }

            try (FileChannel channel = FileChannel.open(file, StandardOpenOption.READ)) {
                MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, FILE_SIZE);
                double[] channelTimes = new double[5];
                double[] mappedTimes = new double[5];
                long sumByChannel = 0;
                long sumByMapped = 0;
                for (int i = -1; i < 5; i++) {  // 最初の1回（i = -1）は計らない
                    long start = System.nanoTime();
                    sumByChannel = byChannel(channel, positions);
                    long middle = System.nanoTime();
                    sumByMapped = byMapped(mapped, positions);
                    long end = System.nanoTime();
                    if (i >= 0) {
                        channelTimes[i] = (middle - start) / 1e6;
                        mappedTimes[i] = (end - middle) / 1e6;
                    }
                }
                Arrays.sort(channelTimes);
                Arrays.sort(mappedTimes);
                System.out.printf("チャネルの read: %.1f ms%n", channelTimes[2]);
                System.out.printf("メモリマップ: %.1f ms%n", mappedTimes[2]);
                System.out.println("読んだ値の合計が一致: " + (sumByChannel == sumByMapped));
            }
        } finally {
            Files.delete(file);
        }
    }
}
