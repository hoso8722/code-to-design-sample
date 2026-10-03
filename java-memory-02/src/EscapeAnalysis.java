// 実行: java EscapeAnalysis.java
import java.lang.management.ManagementFactory;

public class EscapeAnalysis {
    record Point(int x, int y) {}

    static Point last;  // escape() で作った Point を入れておくフィールド

    // Point はこのメソッドの外に出ない
    static long noEscape(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            Point p = new Point(i, i + 1);
            sum += p.x() + p.y();
        }
        return sum;
    }

    // Point をフィールドに入れるので、メソッドの外に出る
    static long escape(int n) {
        long sum = 0;
        for (int i = 0; i < n; i++) {
            Point p = new Point(i, i + 1);
            last = p;
            sum += p.x() + p.y();
        }
        return sum;
    }

    // このスレッドがこれまでにヒープに確保したバイト数
    static long allocatedBytes() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return bean.getCurrentThreadAllocatedBytes();
    }

    public static void main(String[] args) {
        int n = 10_000_000;
        System.out.println("round,noEscape_MB,escape_MB");
        for (int round = 1; round <= 10; round++) {
            long before = allocatedBytes();
            noEscape(n);
            long middle = allocatedBytes();
            escape(n);
            long after = allocatedBytes();
            System.out.printf("%d,%.1f,%.1f%n", round,
                    (middle - before) / 1e6, (after - middle) / 1e6);
        }
    }
}
