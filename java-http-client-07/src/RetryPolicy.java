// 実行: java RetryPolicy.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryPolicy {
    record Policy(int maxAttempts, Duration baseDelay, Duration maxDelay, Duration totalDeadline) {}

    static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 502, 503, 504);
    static final long START = System.nanoTime();

    static void log(String message) {
        System.out.printf("%5d ms  %s%n", (System.nanoTime() - START) / 1_000_000, message);
    }

    // 待ち時間は 基準 × 2^(回数-1) で増やし、上限で頭打ちにする
    static Duration backoff(Policy policy, int attempt) {
        long millis = policy.baseDelay().toMillis() << (attempt - 1);
        return Duration.ofMillis(Math.min(millis, policy.maxDelay().toMillis()));
    }

    static Optional<Duration> retryAfter(HttpResponse<?> response) {
        return response.headers().firstValue("Retry-After").flatMap(v -> {
            try {
                return Optional.of(Duration.ofSeconds(Long.parseLong(v.trim())));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }

    // 冪等なGETだけを対象にした再試行
    static String getWithRetry(HttpClient client, URI uri, Policy policy) throws IOException, InterruptedException {
        long deadline = System.nanoTime() + policy.totalDeadline().toNanos();
        for (int attempt = 1; ; attempt++) {
            Duration wait;
            try {
                HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofMillis(500)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                log("  試行" + attempt + ": status=" + status);
                if (status >= 200 && status < 300) {
                    return response.body();
                }
                if (!RETRYABLE_STATUS.contains(status)) {
                    throw new IOException("再試行しない応答: status=" + status);
                }
                wait = retryAfter(response).orElse(backoff(policy, attempt));
            } catch (HttpTimeoutException | ConnectException e) {
                log("  試行" + attempt + ": " + e.getClass().getSimpleName());
                wait = backoff(policy, attempt);
            }
            if (attempt >= policy.maxAttempts()) {
                throw new IOException("回数の上限(" + policy.maxAttempts() + "回)に達した");
            }
            if (System.nanoTime() + wait.toNanos() > deadline) {
                throw new IOException("総期限(" + policy.totalDeadline().toMillis() + "ms)を超えるので打ち切る");
            }
            log("  " + wait.toMillis() + " ms 待ってから再試行");
            Thread.sleep(wait);
        }
    }

    static void reply(HttpExchange exchange, int status, String text, String retryAfter) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        if (retryAfter != null) {
            exchange.getResponseHeaders().set("Retry-After", retryAfter);
        }
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    public static void main(String[] args) throws Exception {
        AtomicInteger flaky = new AtomicInteger();
        AtomicInteger limited = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // 最初の2回は503、3回目で成功
        server.createContext("/flaky", exchange ->
                reply(exchange, flaky.incrementAndGet() <= 2 ? 503 : 200, "flaky-ok", null));
        // 最初の1回は429(Retry-After: 1)、2回目で成功
        server.createContext("/limited", exchange -> {
            if (limited.incrementAndGet() == 1) {
                reply(exchange, 429, "too many requests", "1");
            } else {
                reply(exchange, 200, "limited-ok", null);
            }
        });
        server.createContext("/always503", exchange -> reply(exchange, 503, "down", null));
        server.createContext("/missing", exchange -> reply(exchange, 404, "not found", null));
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();
        Policy policy = new Policy(4, Duration.ofMillis(100), Duration.ofMillis(400), Duration.ofSeconds(3));
        Policy shortDeadline = new Policy(10, Duration.ofMillis(100), Duration.ofMillis(400), Duration.ofMillis(500));

        try (HttpClient client = HttpClient.newHttpClient()) {
            run(client, "/flaky(503が2回、そのあと成功)", base + "/flaky", policy);
            run(client, "/limited(429 Retry-After: 1、そのあと成功)", base + "/limited", policy);
            run(client, "/always503(ずっと503、回数の上限4回)", base + "/always503", policy);
            run(client, "/always503(ずっと503、総期限500ms)", base + "/always503", shortDeadline);
            run(client, "/missing(404)", base + "/missing", policy);
        } finally {
            server.stop(0);
        }
    }

    static void run(HttpClient client, String label, String url, Policy policy) throws InterruptedException {
        log(label);
        try {
            log("  -> 結果: " + getWithRetry(client, URI.create(url), policy));
        } catch (IOException e) {
            log("  -> 失敗: " + e.getMessage());
        }
    }
}
