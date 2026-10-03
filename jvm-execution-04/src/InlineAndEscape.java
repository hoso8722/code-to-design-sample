// 実行: java InlineAndEscape.java
import java.lang.management.ManagementFactory;

public class InlineAndEscape {
    record Point(int x, int y) {}

    // Point を作って返すだけの小さなメソッド
    static Point makePoint(int i) {
        return new Point(i, i + 1);
    }

    static long sum(int n) {
        long s = 0;
        for (int i = 0; i < n; i++) {
            Point p = makePoint(i);  // 呼び出し先で作ったオブジェクトを、ここで使う
            s += p.x() + p.y();
        }
        return s;
    }

    static long allocatedBytes() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return bean.getCurrentThreadAllocatedBytes();
    }

    public static void main(String[] args) {
        long check = 0;
        for (int i = 0; i < 20; i++) check += sum(1_000_000);  // 準備運動（JITコンパイルさせる）
        long before = allocatedBytes();
        check += sum(10_000_000);
        System.out.printf("Point を1,000万個作ったときの確保量: %.1f MB（check=%d）%n", (allocatedBytes() - before) / 1e6, check);
    }
}
