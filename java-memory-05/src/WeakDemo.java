// 実行: java WeakDemo.java weak
import java.lang.management.ManagementFactory;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class WeakDemo {
    record Key(int id) {}
    record Value(Key key, byte[] data) {}  // key に null 以外を入れると、値の中からキーを参照する

    static long usedAfterGcMB() {
        System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "weak";
        Map<Key, Value> map = mode.equals("hash") ? new HashMap<>() : new WeakHashMap<>();
        boolean valueHasKey = mode.equals("weak-value-key");

        // 10万個のキーで値（1KB）を入れる。キーへの強い参照は、最初の100個だけ残す
        List<Key> kept = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            Key key = new Key(i);
            if (i < 100) kept.add(key);
            map.put(key, new Value(valueHasKey ? key : null, new byte[1024]));
        }

        long afterGc = usedAfterGcMB();
        Thread.sleep(200);  // GCで消えた参照の後始末（JVMのスレッドが行う）を少し待つ
        int size = map.size();  // map を操作する
        long afterAccess = usedAfterGcMB();
        System.out.printf("%s: GC後=%dMB  size()=%d  size()のあとGC=%dMB%n", mode, afterGc, size, afterAccess);

        Reference.reachabilityFence(map);  // 最後まで map と kept を生かしておく
        Reference.reachabilityFence(kept);
    }
}
