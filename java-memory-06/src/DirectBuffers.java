// 実行: java DirectBuffers.java 256
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DirectBuffers {
    static final long MB = 1024 * 1024;

    static long rssMB() throws Exception {
        for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
            if (line.startsWith("VmRSS:")) return Long.parseLong(line.replaceAll("[^0-9]", "")) / 1024;
        }
        throw new IllegalStateException("VmRSS が見つかりません");
    }

    static long directMB() {
        for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            if (pool.getName().equals("direct")) return pool.getMemoryUsed() / MB;
        }
        throw new IllegalStateException("direct のバッファプールが見つかりません");
    }

    static void print(int allocatedMB) throws Exception {
        System.gc();
        long heapUsed = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / MB;
        System.out.printf("%d,%d,%d,%d%n", allocatedMB, heapUsed, directMB(), rssMB());
    }

    public static void main(String[] args) throws Exception {
        int totalMB = args.length > 0 ? Integer.parseInt(args[0]) : 256;
        List<ByteBuffer> buffers = new ArrayList<>();
        System.out.println("allocated_mb,heap_used_mb,direct_mb,rss_mb");
        print(0);
        for (int i = 1; i <= totalMB / 16; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect((int) (16 * MB));  // ヒープの外に16MB
            while (buffer.hasRemaining()) buffer.put((byte) 1);             // 書き込んで物理メモリを使わせる
            buffers.add(buffer);
            if (i % 2 == 0) print(i * 16);
        }
    }
}
