// 実行: java BodyCharset.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BodyCharset {
    // 本文はどれもUTF-8のバイト列。Content-Typeの宣言だけを変える
    static void respond(HttpExchange exchange, String contentType) throws java.io.IOException {
        byte[] body = "日本語".getBytes(StandardCharsets.UTF_8);
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/declared", exchange -> respond(exchange, "text/plain; charset=utf-8"));
        server.createContext("/none", exchange -> respond(exchange, null));
        server.createContext("/wrong", exchange -> respond(exchange, "text/plain; charset=ISO-8859-1"));
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/declared", "/none", "/wrong"}) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(base + path)).build();
                String byDefault = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                String forced = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body();
                System.out.printf("%-9s ofString()=%s  ofString(UTF_8)=%s%n", path, byDefault, forced);
            }
        }
        server.stop(0);
    }
}
