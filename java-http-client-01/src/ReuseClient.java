// 実行: java ReuseClient.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReuseClient {
    public static void main(String[] args) throws Exception {
        Set<Integer> clientPorts = ConcurrentHashMap.newKeySet();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ping", exchange -> {
            clientPorts.add(exchange.getRemoteAddress().getPort());
            byte[] body = "pong".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        HttpRequest request = HttpRequest.newBuilder(
                URI.create("http://localhost:" + server.getAddress().getPort() + "/ping")).build();

        try (HttpClient shared = HttpClient.newHttpClient()) {
            for (int i = 0; i < 5; i++) {
                shared.send(request, HttpResponse.BodyHandlers.discarding());
            }
        }
        System.out.println("1つのHttpClientで5回送った: サーバーが見た接続元ポートは " + clientPorts.size() + " 個");

        clientPorts.clear();
        for (int i = 0; i < 5; i++) {
            try (HttpClient perRequest = HttpClient.newHttpClient()) {
                perRequest.send(request, HttpResponse.BodyHandlers.discarding());
            }
        }
        System.out.println("毎回HttpClientを作って5回送った: サーバーが見た接続元ポートは " + clientPorts.size() + " 個");
        server.stop(0);
    }
}
