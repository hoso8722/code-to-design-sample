// 実行: java AsyncBasics.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncBasics {
    static final long START = System.nanoTime();

    static long now() {
        return (System.nanoTime() - START) / 1_000_000;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        server.setExecutor(pool);
        server.createContext("/item", exchange -> {
            try {
                Thread.sleep(300); // 遅い応答を模擬する
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "keyboard".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + server.getAddress().getPort() + "/item")).build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            client.send(request, HttpResponse.BodyHandlers.discarding()); // ウォームアップ(最初の接続のコストを除く)

            // 同期:応答が届くまで戻らない
            long t0 = now();
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("send       : 戻るまで " + (now() - t0) + " ms");

            // 非同期:すぐ戻り、結果は CompletableFuture に入る
            long t1 = now();
            CompletableFuture<String> future = client
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("thenApply  : 実行したスレッド = " + Thread.currentThread().getName());
                        return response.body().toUpperCase();
                    });
            System.out.println("sendAsync  : 戻るまで " + (now() - t1) + " ms, isDone=" + future.isDone());
            System.out.println("main       : 呼び出したスレッド = " + Thread.currentThread().getName());
            String body = future.join();
            System.out.println("join       : 結果 " + body + " (呼び出しから " + (now() - t1) + " ms)");
        } finally {
            server.stop(0);
            pool.shutdownNow();
        }
    }
}
