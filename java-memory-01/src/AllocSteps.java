// 実行: java AllocSteps.java
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// 16MBずつ（64KBの配列を256個ずつ）確保して保持し、ヒープの used / committed と RSS をCSVで出力する
public class AllocSteps {
    static final long MB = 1024 * 1024;

    static long rssBytes() throws Exception {
        for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
            if (line.startsWith("VmRSS:")) {
                return Long.parseLong(line.replaceAll("[^0-9]", "")) * 1024;
            }
        }
        throw new IllegalStateException("VmRSS が見つかりません");
    }

    public static void main(String[] args) throws Exception {
        int steps = args.length > 0 ? Integer.parseInt(args[0]) : 32;
        Runtime rt = Runtime.getRuntime();
        List<byte[]> keep = new ArrayList<>();
        System.out.println("allocated_mb,used_mb,committed_mb,rss_mb");
        for (int i = 0; i <= steps; i++) {
            if (i > 0) {
                for (int j = 0; j < 256; j++) {
                    byte[] block = new byte[64 * 1024];
                    java.util.Arrays.fill(block, (byte) 1);  // 書き込んで物理メモリを使わせる
                    keep.add(block);
                }
            }
            long committed = rt.totalMemory();
            long used = committed - rt.freeMemory();
            System.out.printf("%d,%d,%d,%d%n", i * 16, used / MB, committed / MB, rssBytes() / MB);
        }
    }
}
