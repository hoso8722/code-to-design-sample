// 実行: java Basic.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Basic {
    public static void main(String[] args) throws Exception {
        // JDK に含まれる簡易的な HTTP サーバーを、空いているポートで起動する
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/students/A", exchange -> {
            byte[] body = "Aさん,70,65,80".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/results", exchange -> {
            String received = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = ("登録しました: " + received).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            // GET：受験者のデータを取得する
            HttpRequest get = HttpRequest.newBuilder(URI.create(base + "/students/A")).GET().build();
            HttpResponse<String> got = client.send(get, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET  " + got.statusCode() + " " + got.body());

            // POST：結果を送る
            HttpRequest post = HttpRequest.newBuilder(URI.create(base + "/results"))
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString("Aさん,合格"))
                .build();
            HttpResponse<String> posted = client.send(post, HttpResponse.BodyHandlers.ofString());
            System.out.println("POST " + posted.statusCode() + " " + posted.body());

            // 存在しないパス：例外にはならず、ステータスコードで分かる
            HttpRequest missing = HttpRequest.newBuilder(URI.create(base + "/unknown")).GET().build();
            HttpResponse<String> notFound = client.send(missing, HttpResponse.BodyHandlers.ofString());
            System.out.println("GET  " + notFound.statusCode() + "（存在しないパス）");
        } finally {
            server.stop(0);
        }
    }
}
