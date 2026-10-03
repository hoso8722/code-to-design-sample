// 実行: java Waiting.java
import com.sun.net.httpserver.HttpServer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Waiting {
    static final int REQUESTS = 1_000;
    static final AtomicInteger active = new AtomicInteger();     // サーバーが、今処理している要求の数
    static final AtomicInteger maxActive = new AtomicInteger();  // その最大値

    // executor の上で、REQUESTS 回の要求を送る。limit が 0 より大きければ、同時に送る数を limit までに制限する
    static void run(String name, ExecutorService executor, int limit, HttpClient client, HttpRequest request)
            throws Exception {
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        threads.resetPeakThreadCount();
        maxActive.set(0);
        Semaphore semaphore = limit > 0 ? new Semaphore(limit) : null;

        long start = System.nanoTime();
        List<Future<Integer>> futures = new ArrayList<>();
        try (executor) {
            for (int i = 0; i < REQUESTS; i++) {
                futures.add(executor.submit(() -> {
                    if (semaphore != null) {
                        semaphore.acquire();
                    }
                    try {
                        return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
                    } finally {
                        if (semaphore != null) {
                            semaphore.release();
                        }
                    }
                }));
            }
            int ok = 0;
            for (Future<Integer> future : futures) {
                if (future.get() == 200) {
                    ok++;
                }
            }
            System.out.printf("%s: %.0f ms（成功 %d 件、OSのスレッドの最大数 %d、サーバーの同時処理の最大数 %d）%n",
                name, (System.nanoTime() - start) / 1e6, ok, threads.getPeakThreadCount(), maxActive.get());
        }
    }

    public static void main(String[] args) throws Exception {
        // 応答までに200ミリ秒かかるサーバー。要求ごとに仮想スレッドを使うので、多くの要求を同時に処理できる
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 2_000);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", exchange -> {
            int now = active.incrementAndGet();
            maxActive.accumulateAndGet(now, Math::max);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            active.decrementAndGet();
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://localhost:" + server.getAddress().getPort() + "/")).build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            client.send(request, HttpResponse.BodyHandlers.ofString());  // 計る前に、1回送っておく

            run("スレッドプール（50本）", Executors.newFixedThreadPool(50), 0, client, request);
            run("スレッドプール（200本）", Executors.newFixedThreadPool(200), 0, client, request);
            run("仮想スレッド", Executors.newVirtualThreadPerTaskExecutor(), 0, client, request);
            run("仮想スレッド（同時に100件まで）", Executors.newVirtualThreadPerTaskExecutor(), 100, client, request);
        } finally {
            server.stop(0);
        }
    }
}
