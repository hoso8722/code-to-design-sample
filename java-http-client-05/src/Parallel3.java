// 実行: java Parallel3.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parallel3 {
    static long origin;

    static long now() {
        return (System.nanoTime() - origin) / 1_000_000;
    }

    static HttpServer startServer(boolean multiThreaded, ExecutorService pool) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        if (multiThreaded) {
            server.setExecutor(pool);
        } // 指定しなければ、サーバーは1本のスレッドで順に処理する
        server.createContext("/item", exchange -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        return server;
    }

    static void run(String mode, boolean async, boolean multiThreaded) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        HttpServer server = startServer(multiThreaded, pool);
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/item");
        try (HttpClient client = HttpClient.newHttpClient()) {
            // ウォームアップ(最初の接続のコストを除く)
            client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.discarding());
            origin = System.nanoTime();
            List<String> names = List.of("A", "B", "C");
            if (!async) {
                for (String name : names) {
                    long start = now();
                    client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.discarding());
                    System.out.println(mode + "," + name + "," + start + "," + now());
                }
            } else {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (String name : names) {
                    long start = now();
                    futures.add(client.sendAsync(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.discarding())
                            .thenAccept(r -> System.out.println(mode + "," + name + "," + start + "," + now())));
                }
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
            }
            System.out.println("# " + mode + " 全体: " + now() + " ms");
        } finally {
            server.stop(0);
            pool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        run("sequential", false, true);
        run("async-parallel-server", true, true);
        run("async-single-thread-server", true, false);
    }
}
