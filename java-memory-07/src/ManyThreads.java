// 実行: java ManyThreads.java platform 1000 10
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ManyThreads {
    static long rssMB() throws Exception {
        for (String line : Files.readAllLines(Path.of("/proc/self/status"))) {
            if (line.startsWith("VmRSS:")) return Long.parseLong(line.replaceAll("[^0-9]", "")) / 1024;
        }
        throw new IllegalStateException("VmRSS が見つかりません");
    }

    static long heapUsedMB() {
        System.gc();
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    // depth 回だけ自分を呼んでから、release が開くまで待つ
    static void descend(int depth, CountDownLatch ready, CountDownLatch release) throws InterruptedException {
        if (depth > 0) {
            descend(depth - 1, ready, release);
            return;
        }
        ready.countDown();
        release.await();
    }

    public static void main(String[] args) throws Exception {
        String kind = args.length > 0 ? args[0] : "platform";  // platform または virtual
        int count = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
        int depth = args.length > 2 ? Integer.parseInt(args[2]) : 10;

        long rssBefore = rssMB();
        long heapBefore = heapUsedMB();

        CountDownLatch ready = new CountDownLatch(count);
        CountDownLatch release = new CountDownLatch(1);
        Thread.Builder builder = kind.equals("virtual") ? Thread.ofVirtual() : Thread.ofPlatform();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            threads.add(builder.start(() -> {
                try {
                    descend(depth, ready, release);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }
        ready.await();  // すべてのスレッドが待ち状態になるまで待つ

        long rssAfter = rssMB();
        long heapAfter = heapUsedMB();
        System.out.printf("%s,%d,%d,%d,%d%n", kind, count, depth, rssAfter - rssBefore, heapAfter - heapBefore);

        if (args.length > 3 && args[3].equals("wait")) {
            System.out.println("pid=" + ProcessHandle.current().pid());
            Thread.sleep(30_000);  // この間に jcmd で調べる
        }
        release.countDown();
        for (Thread t : threads) t.join();
    }
}
