// 実行: java NaiveRetry.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NaiveRetry {
    public static void main(String[] args) throws Exception {
        AtomicInteger created = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(pool);
        // 注文を受けたらすぐ1件作り、応答までに700ミリ秒かかる(重い後処理を模擬)
        server.createContext("/orders", exchange -> {
            int id = created.incrementAndGet();
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = ("order-" + id).getBytes(StandardCharsets.UTF_8);
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
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(300))
                    .POST(HttpRequest.BodyPublishers.ofString("sku=42&quantity=1"))
                    .build();
            // 失敗したら、とにかく3回まで送り直す
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("試行" + attempt + ": 成功 " + response.body());
                    break;
                } catch (HttpTimeoutException e) {
                    System.out.println("試行" + attempt + ": タイムアウト");
                }
            }
            Thread.sleep(1000); // サーバー側の処理が終わるのを待つ
            System.out.println("サーバーが作った注文: " + created.get() + " 件");
        } finally {
            server.stop(0);
            pool.shutdownNow();
        }
    }
}
