// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

// 試行ごとに新しいクラスローダーでクラスを読み込み直し、8本のスレッドが同時に getInstance を呼んだときの、インスタンスの数を数える
public class Race {
    static final int TRIALS = 2000;
    static final int THREADS = 8;

    public static void main(String[] args) throws Exception {
        URL location = Race.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println("試行 " + TRIALS + " 回、スレッド " + THREADS + " 本");
        for (String name : new String[] {"LazyUnsafe", "LazyUnsafeLight", "SyncLazy", "Eager", "Holder", "EnumConfig"}) {
            int duplicated = 0;
            int maxInstances = 1;
            for (int t = 0; t < TRIALS; t++) {
                int n = countInstances(location, "exam." + name);
                if (n > 1) duplicated++;
                maxInstances = Math.max(maxInstances, n);
            }
            System.out.printf("%-11s インスタンスが2つ以上できた試行: %4d / %d（最大 %d 個）%n", name, duplicated, TRIALS, maxInstances);
        }
    }

    static int countInstances(URL location, String className) throws Exception {
        try (URLClassLoader loader = new URLClassLoader(new URL[] {location}, ClassLoader.getPlatformClassLoader())) {
            Class<?> type = loader.loadClass(className);
            var method = type.getMethod("getInstance");
            Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            CountDownLatch ready = new CountDownLatch(THREADS);
            CountDownLatch go = new CountDownLatch(1);
            AtomicInteger errors = new AtomicInteger();
            Thread[] threads = new Thread[THREADS];
            for (int i = 0; i < THREADS; i++) {
                threads[i] = new Thread(() -> {
                    try {
                        ready.countDown();
                        go.await();
                        Object instance = method.invoke(null);
                        synchronized (seen) { seen.add(instance); }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                });
                threads[i].start();
            }
            ready.await();
            go.countDown();
            for (Thread th : threads) th.join();
            return seen.size();
        }
    }
}
