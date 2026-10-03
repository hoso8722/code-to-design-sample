// 実行: java AllocSize.java
import java.lang.management.ManagementFactory;
import java.util.function.IntFunction;

public class AllocSize {
    record Point(int x, int y) {}

    static Object sink;  // 作ったオブジェクトを入れて、エスケープ解析で消えないようにする

    static long allocatedBytes() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return bean.getCurrentThreadAllocatedBytes();
    }

    // factory で n 個作ったときの、1個あたりの確保バイト数
    static double bytesPerObject(IntFunction<Object> factory) {
        int n = 1_000_000;
        for (int i = 0; i < n; i++) sink = factory.apply(i);  // 準備運動（JITコンパイルさせる）
        long before = allocatedBytes();
        for (int i = 0; i < n; i++) sink = factory.apply(i);
        return (double) (allocatedBytes() - before) / n;
    }

    public static void main(String[] args) {
        System.out.printf("new Object()           : %5.1f bytes%n", bytesPerObject(i -> new Object()));
        System.out.printf("Integer.valueOf(i%%100) : %5.1f bytes%n", bytesPerObject(i -> Integer.valueOf(i % 100)));
        System.out.printf("Integer.valueOf(1000+i): %5.1f bytes%n", bytesPerObject(i -> Integer.valueOf(1000 + i)));
        System.out.printf("Long.valueOf(1000+i)   : %5.1f bytes%n", bytesPerObject(i -> Long.valueOf(1000 + i)));
        System.out.printf("new Point(i, i)        : %5.1f bytes%n", bytesPerObject(i -> new Point(i, i)));
        System.out.printf("new int[0]             : %5.1f bytes%n", bytesPerObject(i -> new int[0]));
        System.out.printf("new int[10]            : %5.1f bytes%n", bytesPerObject(i -> new int[10]));
        System.out.printf("new byte[10]           : %5.1f bytes%n", bytesPerObject(i -> new byte[10]));
    }
}
