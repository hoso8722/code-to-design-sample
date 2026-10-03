// 実行: java StringSize.java
import java.lang.management.ManagementFactory;

// 長さ n の文字列1個を作るのに確保するバイト数（String オブジェクト + 中の byte[]）
public class StringSize {
    static Object sink;

    static long allocatedBytes() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return bean.getCurrentThreadAllocatedBytes();
    }

    static double bytesPerString(String unit, int length) {
        int n = 100_000;
        for (int i = 0; i < n; i++) sink = unit.repeat(length);  // 準備運動
        long before = allocatedBytes();
        for (int i = 0; i < n; i++) sink = unit.repeat(length);
        return (double) (allocatedBytes() - before) / n;
    }

    public static void main(String[] args) {
        System.out.println("length,latin1_bytes,japanese_bytes");
        for (int length : new int[] {2, 4, 8, 16, 32, 64, 100}) {
            System.out.printf("%d,%.1f,%.1f%n", length,
                    bytesPerString("a", length),    // ASCII（Latin-1 で表せる）
                    bytesPerString("あ", length));  // 日本語（Latin-1 で表せない）
        }
    }
}
