// 実行: java ChainAndErrors.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChainAndErrors {
    static void reply(com.sun.net.httpserver.HttpExchange exchange, String text, long delayMs) throws java.io.IOException {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        try {
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
        } catch (java.io.IOException ignored) {
            // クライアントが先に切った場合
        }
        exchange.close();
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        ExecutorService pool = Executors.newFixedThreadPool(4);
        server.setExecutor(pool);
        server.createContext("/user", exchange -> reply(exchange, "u-7", 100));
        server.createContext("/orders", exchange -> reply(exchange, "3 orders for " + exchange.getRequestURI().getQuery(), 100));
        server.createContext("/hang", exchange -> reply(exchange, "late", 2000));
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            // thenCompose:1つ目の結果(ユーザーID)を使って、2つ目の要求を送る
            CompletableFuture<String> orders = client
                    .sendAsync(HttpRequest.newBuilder(URI.create(base + "/user")).build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenCompose(userId -> client.sendAsync(
                            HttpRequest.newBuilder(URI.create(base + "/orders?user=" + userId)).build(),
                            HttpResponse.BodyHandlers.ofString()))
                    .thenApply(HttpResponse::body);
            System.out.println("thenCompose : " + orders.join());

            // 途中で失敗した場合:例外は後ろの段へ伝わり、exceptionally で代わりの値にできる
            HttpRequest slow = HttpRequest.newBuilder(URI.create(base + "/hang"))
                    .timeout(Duration.ofMillis(300)).build();
            CompletableFuture<String> failing = client
                    .sendAsync(slow, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        System.out.println("  (この thenApply は実行されない)");
                        return response.body();
                    });
            String fallback = failing
                    .exceptionally(ex -> {
                        System.out.println("exceptionally に届いた例外 : " + ex.getClass().getSimpleName()
                                + " / 原因 : " + ex.getCause().getClass().getSimpleName());
                        return "代わりの値";
                    })
                    .join();
            System.out.println("exceptionally の結果 : " + fallback);

            // join は CompletionException で包んで投げる
            try {
                failing.join();
            } catch (CompletionException e) {
                System.out.println("join が投げた例外 : " + e.getClass().getSimpleName() + " / 原因 : " + e.getCause().getClass().getSimpleName());
            }
            // get は ExecutionException で包んで投げる
            try {
                failing.get();
            } catch (java.util.concurrent.ExecutionException e) {
                System.out.println("get が投げた例外  : " + e.getClass().getSimpleName() + " / 原因 : " + e.getCause().getClass().getSimpleName());
            }
        } finally {
            server.stop(0);
            pool.shutdownNow();
        }
    }
}
