// 実行: java HttpHello.java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

// HTTP のクライアントとリクエストを「作るだけ」で、送信はしない（通信の時間を含めないため）
public class HttpHello {
    public static void main(String[] args) {
        try (HttpClient client = HttpClient.newHttpClient()) {  // JDK 21 以降は close() できる
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://example.com/")).GET().build();
            System.out.println(client.version() + " " + request.method() + " " + request.uri());
        }
    }
}
