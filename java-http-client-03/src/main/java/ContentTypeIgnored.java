// 実行: mvn -q compile exec:java -Dexec.mainClass=ContentTypeIgnored
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ContentTypeIgnored {
    record Product(String id, String name) {}

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // JSONの本文を、わざと text/html で返す
        server.createContext("/mislabeled", exchange -> {
            byte[] body = "{\"id\":\"42\",\"name\":\"キーボード\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        // エラーページ(HTML)を、ステータス200で返す
        server.createContext("/errorpage", exchange -> {
            byte[] body = "<html><body>メンテナンス中です</body></html>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/mislabeled", "/errorpage"}) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(base + path)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String contentType = response.headers().firstValue("Content-Type").orElse("");
                System.out.println(path + " Content-Type=" + contentType);
                try {
                    System.out.println("  Jacksonで読む -> " + mapper.readValue(response.body(), Product.class));
                } catch (Exception e) {
                    System.out.println("  Jacksonで読む -> " + e.getClass().getSimpleName());
                }
                System.out.println("  JSONと宣言されているか -> " + contentType.startsWith("application/json"));
            }
        } finally {
            server.stop(0);
        }
    }
}
