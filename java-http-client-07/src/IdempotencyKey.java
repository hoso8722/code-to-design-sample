// 実行: java IdempotencyKey.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class IdempotencyKey {
    public static void main(String[] args) throws Exception {
        AtomicInteger created = new AtomicInteger();
        ConcurrentHashMap<String, String> orderByKey = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(pool);
        // 同じ Idempotency-Key の要求は、最初の1件だけ処理し、結果を使い回す
        server.createContext("/orders", exchange -> {
            String key = exchange.getRequestHeaders().getFirst("Idempotency-Key");
            String order = orderByKey.computeIfAbsent(key, k -> {
                int id = created.incrementAndGet();
                try {
                    Thread.sleep(700); // 重い後処理を模擬
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "order-" + id;
            });
            byte[] body = order.getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(201, body.length);
                exchange.getResponseBody().write(body);
            } catch (java.io.IOException ignored) {
                // クライアントが先に切った場合
            }
            exchange.close();
        });
        server.start();
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/orders");

        try (HttpClient client = HttpClient.newHttpClient()) {
            String key = UUID.randomUUID().toString(); // 「この注文」を表す。再送でも同じ値を使う
            for (int attempt = 1; attempt <= 3; attempt++) {
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofMillis(300))
                        .header("Idempotency-Key", key)
                        .POST(HttpRequest.BodyPublishers.ofString("sku=42&quantity=1"))
                        .build();
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("試行" + attempt + ": 成功 " + response.body());
                    break;
                } catch (HttpTimeoutException e) {
                    System.out.println("試行" + attempt + ": タイムアウト");
                }
            }
            Thread.sleep(1000);
            System.out.println("サーバーが作った注文: " + created.get() + " 件");
        } finally {
            server.stop(0);
            pool.shutdownNow();
        }
    }
}
