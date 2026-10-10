// 実行: mvn -q compile exec:java -Dexec.mainClass=JsonClient
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class JsonClient {
    record Product(String id, String name) {}
    record Order(String productId, int quantity) {}
    record Receipt(String result) {}

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/product", exchange -> {
            byte[] body = mapper.writeValueAsBytes(new Product("42", "キーボード"));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/orders", exchange -> {
            String received = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("サーバーが受け取った本文: " + received);
            Order order = mapper.readValue(received, Order.class);
            byte[] body = mapper.writeValueAsBytes(new Receipt(
                    "accepted " + order.productId() + " x " + order.quantity()));
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI base = URI.create("http://localhost:" + server.getAddress().getPort());

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest get = HttpRequest.newBuilder(base.resolve("/product"))
                    .header("Accept", "application/json")
                    .GET().build();
            HttpResponse<String> productResponse = client.send(get, HttpResponse.BodyHandlers.ofString());
            System.out.println("応答の本文: " + productResponse.body());
            Product product = mapper.readValue(productResponse.body(), Product.class);
            System.out.println("record: " + product);

            String json = mapper.writeValueAsString(new Order(product.id(), 2));
            HttpRequest post = HttpRequest.newBuilder(base.resolve("/orders"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> receiptResponse = client.send(post, HttpResponse.BodyHandlers.ofString());
            System.out.println("record: " + mapper.readValue(receiptResponse.body(), Receipt.class));
        } finally {
            server.stop(0);
        }
    }
}
