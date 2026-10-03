// 実行: java Timeouts.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

public class Timeouts {
    public static void main(String[] args) throws Exception {
        // 応答までに2秒かかるサーバー
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/");

        // 接続のタイムアウトは、HttpClient に設定する
        try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build()) {
            // 要求のタイムアウトは、HttpRequest に設定する
            HttpRequest withTimeout = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(500)).build();
            long start = System.nanoTime();
            try {
                client.send(withTimeout, HttpResponse.BodyHandlers.ofString());
            } catch (HttpTimeoutException e) {
                System.out.printf("タイムアウトあり: %d ms 待って %s%n", (System.nanoTime() - start) / 1_000_000, e);
            }

            HttpRequest withoutTimeout = HttpRequest.newBuilder(uri).build();
            start = System.nanoTime();
            HttpResponse<String> response = client.send(withoutTimeout, HttpResponse.BodyHandlers.ofString());
            System.out.printf("タイムアウトなし: %d ms 待って %d %s%n",
                (System.nanoTime() - start) / 1_000_000, response.statusCode(), response.body());
        } finally {
            server.stop(0);
        }
    }
}
