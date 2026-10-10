// 実行: java BodyTimeout.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class BodyTimeout {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // ヘッダーはすぐ返し、本文は1秒おきに5回に分けて送る(全体で約5秒)
        server.createContext("/drip", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            try {
                for (int i = 1; i <= 5; i++) {
                    exchange.getResponseBody().write(("part" + i + " ").getBytes(StandardCharsets.UTF_8));
                    exchange.getResponseBody().flush();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                // クライアントが切った場合は、ここに来る
            }
            exchange.close();
        });
        server.start();

        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/drip");
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(500)).build();
            long start = System.nanoTime();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("例外なし。status=" + response.statusCode() + " 本文=" + response.body().trim());
            System.out.println("send が戻るまで: 約" + ms + " ms(要求のタイムアウトは500ms)");
        } finally {
            server.stop(0);
        }
    }
}
