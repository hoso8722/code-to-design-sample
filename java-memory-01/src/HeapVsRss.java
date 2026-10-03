// 実行: java HeapVsRss.java
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeapVsRss {
    static final long MB = 1024 * 1024;

    // OSから見たこのプロセスのメモリ（Linuxの /proc/self/status の VmRSS）
    static long rssBytes() throws Exception {
        for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
            if (line.startsWith("VmRSS:")) {
                return Long.parseLong(line.replaceAll("[^0-9]", "")) * 1024;
            }
        }
        throw new IllegalStateException("VmRSS が見つかりません");
    }

    static void print(String label) throws Exception {
        Runtime rt = Runtime.getRuntime();
        long committed = rt.totalMemory();        // ヒープとして確保済みの量
        long used = committed - rt.freeMemory();  // そのうち使っている量
        long max = rt.maxMemory();                // ヒープの上限
        System.out.printf("%-10s used=%4d MB  committed=%4d MB  max=%4d MB  RSS=%4d MB%n",
                label, used / MB, committed / MB, max / MB, rssBytes() / MB);
    }

    public static void main(String[] args) throws Exception {
        print("起動直後");

        List<byte[]> keep = new ArrayList<>();
        for (int i = 0; i < 256 * 16; i++) {  // 64KB × 4096個 = 256MB
            byte[] block = new byte[64 * 1024];
            Arrays.fill(block, (byte) 1);      // 書き込んで、物理メモリを使わせる
            keep.add(block);
        }
        print("256MB保持");

        keep.clear();
        System.gc();
        print("GC直後");

        Thread.sleep(1000);
        print("1秒後");
    }
}
