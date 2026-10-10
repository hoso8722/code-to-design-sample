// 実行: java ApiClient.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class ApiClient {
    // 呼び出し側へ伝える結果。応答が返った場合は、ステータスの種類ごとに型を分ける
    sealed interface ApiResult permits Ok, ClientError, RateLimited, ServerError {}
    record Ok(String body) implements ApiResult {}
    record ClientError(int status, String requestId, String bodyHead) implements ApiResult {
        // 本文には秘密が含まれることがあるので、toString(ログに出る文字列)には含めない
        @Override
        public String toString() {
            return "ClientError[status=" + status + ", requestId=" + requestId + "]";
        }
    }
    record RateLimited(Optional<Duration> retryAfter, String requestId) implements ApiResult {}
    record ServerError(int status, String requestId) implements ApiResult {}

    static final int MAX_SUCCESS_BYTES = 1_000_000;
    static final int MAX_ERROR_BYTES = 200;

    private final HttpClient client;

    ApiClient(HttpClient client) {
        this.client = client;
    }

    ApiResult get(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int status = response.statusCode();
        String requestId = response.headers().firstValue("X-Request-Id").orElse("(なし)");
        try (InputStream in = response.body()) {
            if (status >= 200 && status < 300) {
                return new Ok(readUpTo(in, MAX_SUCCESS_BYTES));
            }
            if (status == 429) {
                return new RateLimited(retryAfter(response), requestId);
            }
            if (status >= 400 && status < 500) {
                return new ClientError(status, requestId, readUpTo(in, MAX_ERROR_BYTES));
            }
            // 5xx など。本文は読まずに捨てる(中身を呼び出し側へ渡さない)
            return new ServerError(status, requestId);
        }
    }

    static Optional<Duration> retryAfter(HttpResponse<?> response) {
        return response.headers().firstValue("Retry-After").flatMap(v -> {
            try {
                return Optional.of(Duration.ofSeconds(Long.parseLong(v.trim())));
            } catch (NumberFormatException e) {
                return Optional.empty(); // 日付の形式などは、ここでは扱わない
            }
        });
    }

    // 最大 limit バイトまで読み、超えた分は読まない
    static String readUpTo(InputStream in, int limit) throws IOException {
        byte[] bytes = in.readNBytes(limit);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/ok", exchange -> reply(exchange, 200, "keyboard", null));
        server.createContext("/missing", exchange -> reply(exchange, 404, "{\"error\":\"item not found\"}", "req-404"));
        server.createContext("/limited", exchange -> {
            exchange.getResponseHeaders().set("Retry-After", "30");
            reply(exchange, 429, "{\"error\":\"too many requests\"}", "req-429");
        });
        server.createContext("/bad", exchange ->
                reply(exchange, 400, "invalid token: sk-live-abc123 " + "x".repeat(5000), "req-400"));
        server.createContext("/broken", exchange ->
                reply(exchange, 503, "stack trace: at com.example.Secret.connect(password=hunter2)", "req-503"));
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            ApiClient api = new ApiClient(client);
            for (String path : new String[] {"/ok", "/missing", "/limited", "/bad", "/broken"}) {
                ApiResult result = api.get(URI.create(base + path));
                System.out.println(path + " -> " + result);
                if (result instanceof ClientError error) {
                    System.out.println("    保持した本文の先頭: " + error.bodyHead().length() + " 文字(上限 " + MAX_ERROR_BYTES + " バイト)");
                }
            }
            server.stop(0);
            try {
                api.get(URI.create(base + "/ok"));
            } catch (IOException e) {
                System.out.println("サーバー停止後 -> IOException: " + e.getClass().getSimpleName());
            }
        }
    }

    static void reply(HttpExchange exchange, int status, String text, String requestId) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        if (requestId != null) {
            exchange.getResponseHeaders().set("X-Request-Id", requestId);
        }
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
