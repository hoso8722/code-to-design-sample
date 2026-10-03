// 実行: java FillMemory.java 300 150
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// ヒープに heapMB、ヒープの外（Direct Buffer）に directMB を確保して書き込む
public class FillMemory {
    public static void main(String[] args) throws Exception {
        int heapMB = Integer.parseInt(args[0]);
        int directMB = Integer.parseInt(args[1]);
        System.out.println("max heap = " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");

        List<byte[]> heap = new ArrayList<>();
        for (int i = 0; i < heapMB; i++) {
            byte[] block = new byte[1024 * 1024 - 16];  // 約1MB（ヘッダ込みで1MBに収まる大きさ）
            Arrays.fill(block, (byte) 1);
            heap.add(block);
        }
        System.out.println("heap: " + heapMB + " MB");

        List<ByteBuffer> direct = new ArrayList<>();
        for (int i = 0; i < directMB; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
            while (buffer.hasRemaining()) buffer.put((byte) 1);
            direct.add(buffer);
            if ((i + 1) % 50 == 0) System.out.println("direct: " + (i + 1) + " MB");
        }
        System.out.println("done");
    }
}
