// 実行: java FlipForgot.java
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class FlipForgot {
    static byte[] writeAndRead(Path file, boolean flip) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.put("PASS".getBytes(StandardCharsets.UTF_8));
        if (flip) {
            buffer.flip();
        }
        try (FileChannel channel = FileChannel.open(file,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            channel.write(buffer);  // position から limit までを書く
        }
        return Files.readAllBytes(file);
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("flip", ".bin");
        try {
            System.out.println("flip を呼ばない: " + Arrays.toString(writeAndRead(file, false)));
            System.out.println("flip を呼ぶ: " + Arrays.toString(writeAndRead(file, true)));
        } finally {
            Files.delete(file);
        }
    }
}
