// 実行: java Classify.java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Classify {
    sealed interface Outcome permits Reply, TimedOut, Unreachable {}
    record Reply(int status) implements Outcome {}
    record TimedOut() implements Outcome {}
    record Unreachable(String reason) implements Outcome {}

    static Outcome call(HttpClient client, String url) throws InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMillis(300)).build();
        try {
            return new Reply(client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode());
        } catch (HttpTimeoutException e) {
            return new TimedOut();
        } catch (ConnectException e) {
            return new Unreachable("接続できない");
        } catch (IOException e) {
            return new Unreachable(e.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ok", exchange -> reply(exchange, 200));
        server.createContext("/unavailable", exchange -> reply(exchange, 503));
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            reply(exchange, 200);
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/ok", "/unavailable", "/slow"}) {
                System.out.println(path + " -> " + call(client, base + path));
            }
            server.stop(0);
            System.out.println("(サーバー停止後) /ok -> " + call(client, base + "/ok"));
        }
    }

    static void reply(com.sun.net.httpserver.HttpExchange exchange, int status) throws IOException {
        byte[] body = "x".getBytes(StandardCharsets.UTF_8);
        try {
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
        } catch (IOException ignored) {
            // クライアントが先に切った場合
        }
        exchange.close();
    }
}
