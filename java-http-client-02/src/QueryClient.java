// 実行: java QueryClient.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class QueryClient {
    static String encodeQueryValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/search", exchange -> {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            String decoded = URLDecoder.decode(rawQuery.substring("q=".length()), StandardCharsets.UTF_8);
            byte[] body = (rawQuery + "\t" + decoded).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        List<String> values = List.of("cafe & tea + cake", "日本語", "100%", "a=b#c", "空 白");
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String value : values) {
                URI uri = URI.create(base + "/search?q=" + encodeQueryValue(value));
                HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String[] parts = response.body().split("\t", 2);
                System.out.println("入力: " + value);
                System.out.println("  送ったクエリ: q=" + parts[0].substring(2));
                System.out.println("  サーバーの復号: " + parts[1]);
            }
        }
        server.stop(0);
    }
}
