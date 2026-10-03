// 実行: java Reuse.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Reuse {
    static final int REQUESTS = 200;

    public static void main(String[] args) throws Exception {
        // サーバーは、要求を送ってきた側のポート番号を記録する。接続が違えば、ポート番号も違う
        Set<Integer> clientPorts = ConcurrentHashMap.newKeySet();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            clientPorts.add(exchange.getRemoteAddress().getPort());
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://localhost:" + server.getAddress().getPort() + "/")).build();

        try {
            for (int round = 1; round <= 3; round++) {
                // 1. 要求のたびに、HttpClient を作る
                clientPorts.clear();
                long start = System.nanoTime();
                for (int i = 0; i < REQUESTS; i++) {
                    try (HttpClient client = HttpClient.newHttpClient()) {
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                    }
                }
                double eachTime = (System.nanoTime() - start) / 1e6;
                int eachConnections = clientPorts.size();

                // 2. 1つの HttpClient を使い回す
                clientPorts.clear();
                start = System.nanoTime();
                try (HttpClient client = HttpClient.newHttpClient()) {
                    for (int i = 0; i < REQUESTS; i++) {
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                    }
                }
                double sharedTime = (System.nanoTime() - start) / 1e6;
                int sharedConnections = clientPorts.size();

                System.out.printf("%d回目: 毎回作る %.0f ms（接続 %d 本）、使い回す %.0f ms（接続 %d 本）%n",
                    round, eachTime, eachConnections, sharedTime, sharedConnections);
            }
        } finally {
            server.stop(0);
        }
    }
}
