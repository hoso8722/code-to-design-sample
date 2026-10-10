// 実行: mvn test
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// テスト用のサーバー。localhost の空きポートで起動し、最後に受けた要求を記録する
public class StubServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private volatile String lastMethod;
    private volatile String lastBody;
    private volatile com.sun.net.httpserver.Headers lastHeaders;
    private boolean closed;

    public StubServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setExecutor(executor);
        server.start();
    }

    public URI baseUri() {
        return URI.create("http://localhost:" + server.getAddress().getPort());
    }

    // path に来た要求へ、status と text を返すように登録する。delay の間、応答を待たせる
    public void respond(String path, int status, String text, Duration delay) {
        server.createContext(path, exchange -> {
            lastMethod = exchange.getRequestMethod();
            lastHeaders = exchange.getRequestHeaders();
            lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = text.getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(status, body.length);
                exchange.getResponseBody().write(body);
            } catch (IOException ignored) {
                // クライアントが先に切った場合
            }
            exchange.close();
        });
    }

    public void respond(String path, int status, String text) {
        respond(path, status, text, Duration.ZERO);
    }

    public String lastMethod() {
        return lastMethod;
    }

    public String lastBody() {
        return lastBody;
    }

    public String lastHeader(String name) {
        return lastHeaders.getFirst(name);
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
