// 実行: java BasicClient.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BasicClient {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/items/42", exchange -> {
            byte[] body = "keyboard".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/orders", exchange -> {
            String received = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = ("accepted: " + received).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        URI base = URI.create("http://localhost:" + server.getAddress().getPort());
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest get = HttpRequest.newBuilder(base.resolve("/items/42")).GET().build();
            HttpResponse<String> item = client.send(get, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET " + item.statusCode() + " " + item.body());

            HttpRequest post = HttpRequest.newBuilder(base.resolve("/orders"))
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString("sku=42&quantity=1"))
                    .build();
            HttpResponse<String> order = client.send(post, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST " + order.statusCode() + " " + order.body());
        } finally {
            server.stop(0);
        }
    }
}
