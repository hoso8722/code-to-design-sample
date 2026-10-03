// 実行: java -Xmx256m CacheLeak.java unbounded
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheLeak {
    // リクエストの結果をキャッシュする（のつもりの）Map
    static Map<String, byte[]> cache;

    static byte[] handle(int requestId) {
        String key = "user-" + requestId;          // 毎回ちがうキー
        return cache.computeIfAbsent(key, k -> new byte[1024]);  // 1KBの結果を作って覚えておく
    }

    static long usedAfterGcMB() {
        System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public static void main(String[] args) throws InterruptedException {
        boolean bounded = args.length > 0 && args[0].equals("bounded");
        if (bounded) {
            // 最大1万件。超えたら、いちばん長く使われていないものを捨てる（LRU）
            cache = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                    return size() > 10_000;
                }
            };
        } else {
            cache = new HashMap<>();  // 上限なし
        }

        System.out.println("requests,used_after_gc_mb,cache_size");
        for (int requestId = 0; requestId <= 150_000; requestId++) {
            handle(requestId);
            if (requestId % 10_000 == 0) {
                System.out.printf("%d,%d,%d%n", requestId, usedAfterGcMB(), cache.size());
            }
        }
        if (args.length > 1 && args[1].equals("wait")) {
            System.out.println("pid=" + ProcessHandle.current().pid());
            Thread.sleep(30_000);  // この間に jcmd で調べる
        }
    }
}
