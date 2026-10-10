// 実行: mvn test
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// 商品のAPIを呼ぶクラス。HttpClient、接続先、タイムアウトを外から受け取る
public class CatalogApi {
    private final HttpClient client;
    private final URI base;
    private final Duration timeout;

    public CatalogApi(HttpClient client, URI base, Duration timeout) {
        this.client = client;
        this.base = base;
        this.timeout = timeout;
    }

    public String findItemName(long id) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(base.resolve("/items/" + id))
                .header("Accept", "text/plain")
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("商品を取得できません: status=" + response.statusCode());
        }
        return response.body();
    }

    // 数値だけの本文なので、文字列連結で組み立ててよい
    public String createOrder(long itemId, int quantity) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(base.resolve("/orders"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString("itemId=" + itemId + "&quantity=" + quantity))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 201) {
            throw new IOException("注文できません: status=" + response.statusCode());
        }
        return response.body();
    }
}
