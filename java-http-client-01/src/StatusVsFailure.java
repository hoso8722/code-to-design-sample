// 実行: java StatusVsFailure.java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class StatusVsFailure {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/missing", exchange -> {
            byte[] body = "not found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(404, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/broken", exchange -> {
            byte[] body = "boom".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/missing", "/broken"}) {
                HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + path)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(path + " -> 例外なしで戻った。status=" + response.statusCode() + " body=" + response.body());
            }
        }
        server.stop(0);

        // サーバーを止めたあとの同じポートへ送る
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + port + "/missing")).build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("サーバー停止後 -> " + e.getClass().getName());
        }
    }
}
