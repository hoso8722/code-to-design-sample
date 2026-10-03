// 実行: java NotClosed.java
import com.sun.management.UnixOperatingSystemMXBean;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NotClosed {
    // このプロセスが開いているファイルの数（LinuxやmacOSで使える）
    static long openFiles() {
        return ((UnixOperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
            .getOpenFileDescriptorCount();
    }

    public static void main(String[] args) throws IOException {
        Path file = Files.createTempFile("scores", ".csv");
        Files.writeString(file, "受験者0000001,70,65,80\n");
        List<Stream<String>> opened = new ArrayList<>();
        try {
            System.out.println("最初に開いているファイル: " + openFiles());
            for (int i = 1; i <= 1_000; i++) {
                Stream<String> lines = Files.lines(file);
                lines.count();       // 最後まで読んでも、ファイルは閉じられない
                opened.add(lines);   // 閉じていないストリームを、参照したままにする
            }
            System.out.println("1,000回読んだあと: " + openFiles());
        } catch (IOException | UncheckedIOException e) {
            System.out.println(opened.size() + "個を開いたところで失敗: " + e);
        } finally {
            for (Stream<String> lines : opened) {
                lines.close();
            }
            System.out.println("すべて閉じたあと: " + openFiles());
            Files.delete(file);
        }
    }
}
