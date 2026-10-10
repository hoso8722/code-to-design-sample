// 実行: java NaiveFetch.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class NaiveFetch {
    // 商品名を取得する。本文をそのまま「商品名」として返す
    static String fetchItemName(HttpClient client, URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/items/1", exchange -> reply(exchange, 200, "keyboard"));
        server.createContext("/items/999", exchange -> reply(exchange, 404, "{\"error\":\"item not found\"}"));
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            System.out.println("商品1  : " + fetchItemName(client, URI.create(base + "/items/1")));
            System.out.println("商品999: " + fetchItemName(client, URI.create(base + "/items/999")));
        } finally {
            server.stop(0);
        }
    }

    static void reply(com.sun.net.httpserver.HttpExchange exchange, int status, String text) throws java.io.IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
