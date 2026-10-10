// 実行: java Async.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Async {
    static final int REQUESTS = 20;

    public static void main(String[] args) throws Exception {
        // 応答までに100ミリ秒かかるサーバー。複数の要求を、同時に処理できる
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        ExecutorService serverExecutor = Executors.newCachedThreadPool();
        server.setExecutor(serverExecutor);
        server.createContext("/", exchange -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        HttpRequest request = HttpRequest.newBuilder(
            URI.create("http://localhost:" + server.getAddress().getPort() + "/")).build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            client.send(request, HttpResponse.BodyHandlers.ofString());  // 計る前に、1回送っておく

            for (int round = 1; round <= 3; round++) {
                // 1. send：応答を待ってから、次の要求を送る
                long start = System.nanoTime();
                for (int i = 0; i < REQUESTS; i++) {
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                }
                double syncTime = (System.nanoTime() - start) / 1e6;

                // 2. sendAsync：応答を待たずに、すべての要求を送る
                start = System.nanoTime();
                List<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>();
                for (int i = 0; i < REQUESTS; i++) {
                    futures.add(client.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
                }
                for (CompletableFuture<HttpResponse<String>> future : futures) {
                    future.join();  // すべての応答がそろうまで待つ
                }
                double asyncTime = (System.nanoTime() - start) / 1e6;

                System.out.printf("%d回目: send %.0f ms、sendAsync %.0f ms%n", round, syncTime, asyncTime);
            }
        } finally {
            server.stop(0);
            serverExecutor.shutdownNow();
        }
    }
}
