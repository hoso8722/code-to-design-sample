// 実行: java RequestTimeout.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RequestTimeout {
    static final long START = System.nanoTime();

    static void log(String message) {
        System.out.printf("%5d ms  %s%n", (System.nanoTime() - START) / 1_000_000, message);
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/slow", exchange -> {
            log("サーバー: 処理を始めた(2秒かかる)");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log("サーバー: 処理を終えた");
            byte[] body = "done".getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (java.io.IOException e) {
                log("サーバー: 応答を書けなかった " + e.getClass().getSimpleName());
            }
            exchange.close();
        });
        server.start();

        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/slow");
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(500)).build();
            log("クライアント: 送信(要求のタイムアウトは500ms)");
            try {
                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (HttpTimeoutException e) {
                log("クライアント: " + e.getClass().getSimpleName() + " \"" + e.getMessage() + "\"");
            }
            Thread.sleep(2000);
        } finally {
            server.stop(0);
        }
    }
}
