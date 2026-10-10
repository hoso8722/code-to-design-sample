// 実行: java ResponseParts.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

public class ResponseParts {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/items/42", exchange -> {
            byte[] body = "キーボード".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().set("X-Stock", "12");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/items/42");
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("status  : " + response.statusCode());
            System.out.println("version : " + response.version());
            System.out.println("method  : " + response.request().method());
            System.out.println("path    : " + response.uri().getPath());
            System.out.println("body    : " + response.body());
            System.out.println("headers :");
            new TreeMap<>(response.headers().map()).forEach((name, values) ->
                    System.out.println("  " + name + " = " + values));
            System.out.println("X-Stock : " + response.headers().firstValue("X-Stock").orElse("なし"));
            System.out.println("x-stock : " + response.headers().firstValue("x-stock").orElse("なし"));
            System.out.println("X-STOCK : " + response.headers().firstValue("X-STOCK").orElse("なし"));
            System.out.println("X-Absent: " + response.headers().firstValue("X-Absent").orElse("なし"));
        } finally {
            server.stop(0);
        }
    }
}
