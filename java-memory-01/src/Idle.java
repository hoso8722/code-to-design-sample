// 実行: java -XX:NativeMemoryTracking=summary Idle.java 0
// 別のターミナルで: jcmd <pid> VM.native_memory summary
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Idle {
    public static void main(String[] args) throws Exception {
        int mb = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        List<byte[]> keep = new ArrayList<>();
        for (int i = 0; i < mb * 16; i++) {  // 64KBの配列を mb × 16 個
            byte[] block = new byte[64 * 1024];
            Arrays.fill(block, (byte) 1);
            keep.add(block);
        }
        System.out.println("pid=" + ProcessHandle.current().pid() + " holding " + mb + "MB");
        Thread.sleep(30_000);  // この間に jcmd で調べる
        System.out.println(keep.size());
    }
}
