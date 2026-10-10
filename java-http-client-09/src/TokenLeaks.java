// 実行: java TokenLeaks.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TokenLeaks {
    static final String TOKEN = "demo-token-8f3a"; // 実験用の偽のトークン

    // ログに出すためにヘッダーを文字列にする。Authorization の値は伏せる
    static String redactedHeaders(HttpRequest request) {
        Map<String, java.util.List<String>> sorted = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        sorted.putAll(request.headers().map());
        return sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getKey().equalsIgnoreCase("Authorization") ? "[伏せた]" : e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/data", exchange -> {
            // サーバー側のアクセスログに残るのは、要求行(メソッドとURI)の形
            System.out.println("  [サーバーのアクセスログ] " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort() + "/data";

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest inUrl = HttpRequest.newBuilder(URI.create(base + "?access_token=" + TOKEN)).build();
            HttpRequest inHeader = HttpRequest.newBuilder(URI.create(base))
                    .header("Authorization", "Bearer " + TOKEN).build();

            System.out.println("1. トークンをURLに入れた場合");
            System.out.println("  [クライアントの request.toString()] " + inUrl);
            client.send(inUrl, HttpResponse.BodyHandlers.discarding());

            System.out.println("2. トークンをAuthorizationヘッダーに入れた場合");
            System.out.println("  [クライアントの request.toString()] " + inHeader);
            client.send(inHeader, HttpResponse.BodyHandlers.discarding());

            System.out.println("3. ヘッダーを、そのままログに出した場合と、伏せて出した場合");
            System.out.println("  そのまま: " + inHeader.headers().map());
            System.out.println("  伏せる  : " + redactedHeaders(inHeader));
        } finally {
            server.stop(0);
        }
    }
}
