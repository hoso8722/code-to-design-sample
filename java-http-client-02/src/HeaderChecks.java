// 実行: java HeaderChecks.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HeaderChecks {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/echo", exchange -> {
            String text = "method=" + exchange.getRequestMethod()
                    + " Accept=" + exchange.getRequestHeaders().getFirst("Accept")
                    + " Content-Type=" + exchange.getRequestHeaders().getFirst("Content-Type")
                    + " X-Request-Source=" + exchange.getRequestHeaders().getFirst("X-Request-Source");
            byte[] body = text.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/echo");

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest get = HttpRequest.newBuilder(uri)
                    .header("Accept", "text/plain")
                    .header("X-Request-Source", "catalog-search")
                    .GET().build();
            System.out.println("GET : " + client.send(get, HttpResponse.BodyHandlers.ofString()).body());

            HttpRequest post = HttpRequest.newBuilder(uri)
                    .header("Accept", "text/plain")
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"q\":\"tea\"}")).build();
            System.out.println("POST: " + client.send(post, HttpResponse.BodyHandlers.ofString()).body());
        }

        // 利用者の入力をそのままヘッダー値にしたとき
        String userInput = "web\r\nX-Admin: true";
        try {
            HttpRequest.newBuilder(uri).header("X-Request-Source", userInput).GET().build();
        } catch (IllegalArgumentException e) {
            System.out.println("改行を含む値 -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        try {
            HttpRequest.newBuilder(uri).header("X Bad Name", "v").GET().build();
        } catch (IllegalArgumentException e) {
            System.out.println("空白を含む名前 -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        server.stop(0);
    }
}
