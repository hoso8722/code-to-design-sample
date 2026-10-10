// 実行: java BearerBasic.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class BearerBasic {
    public static void main(String[] args) throws Exception {
        // トークンはソースコードに書かず、環境変数から読む
        String token = System.getenv("API_TOKEN");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("環境変数 API_TOKEN が設定されていません");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/data", exchange -> {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            boolean ok = ("Bearer " + token).equals(authorization);
            byte[] body = (ok ? "hello" : "unauthorized").getBytes(StandardCharsets.UTF_8);
            if (!ok) {
                exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
            }
            exchange.sendResponseHeaders(ok ? 200 : 401, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + "/data");

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest withToken = HttpRequest.newBuilder(uri)
                    .header("Authorization", "Bearer " + token)
                    .build();
            HttpResponse<String> ok = client.send(withToken, HttpResponse.BodyHandlers.ofString());
            System.out.println("正しいトークン: status=" + ok.statusCode() + " body=" + ok.body());

            HttpResponse<String> none = client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("トークンなし  : status=" + none.statusCode()
                    + " WWW-Authenticate=" + none.headers().firstValue("WWW-Authenticate").orElse("なし"));

            HttpRequest wrong = HttpRequest.newBuilder(uri).header("Authorization", "Bearer " + token + "x").build();
            System.out.println("違うトークン  : status=" + client.send(wrong, HttpResponse.BodyHandlers.ofString()).statusCode());
        } finally {
            server.stop(0);
        }
    }
}
