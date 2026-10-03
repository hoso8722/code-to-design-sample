// 実行: java -XX:NativeMemoryTracking=summary OffHeap.java
// 最初の10秒のうちに: jcmd <pid> VM.native_memory baseline
// そのあと30秒のうちに: jcmd <pid> VM.native_memory summary.diff
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OffHeap {
    public static class Template {
        public int value;
        public int twice() { return value * 2; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("pid=" + ProcessHandle.current().pid());
        Thread.sleep(10_000);  // この間に baseline を取る

        // 1. Direct Buffer を256MB（16MB × 16個）
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024 * 1024);
            while (buffer.hasRemaining()) buffer.put((byte) 1);
            buffers.add(buffer);
        }

        // 2. ヒドゥンクラスを1万個
        byte[] bytes;
        try (InputStream in = OffHeap.class.getResourceAsStream("OffHeap$Template.class")) {
            bytes = in.readAllBytes();
        }
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        List<Class<?>> classes = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) classes.add(lookup.defineHiddenClass(bytes, true).lookupClass());

        System.out.println("allocated");
        Thread.sleep(30_000);  // この間に summary.diff を取る
        System.out.println(buffers.size() + " " + classes.size());
    }
}
