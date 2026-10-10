// 実行: mvn test
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogApiTest {
    private StubServer server;
    private HttpClient client;
    private CatalogApi api;

    @BeforeEach
    void setUp() throws IOException {
        server = new StubServer();
        client = HttpClient.newHttpClient();
        api = new CatalogApi(client, server.baseUri(), Duration.ofMillis(300));
    }

    @AfterEach
    void tearDown() {
        client.close();
        server.close();
    }

    @Test
    void 商品名を取得し_Acceptヘッダーを送る() throws Exception {
        server.respond("/items/42", 200, "keyboard");

        assertEquals("keyboard", api.findItemName(42));
        assertEquals("GET", server.lastMethod());
        assertEquals("text/plain", server.lastHeader("Accept"));
    }

    @Test
    void 存在しない商品は例外になり_ステータスが分かる() {
        server.respond("/items/999", 404, "not found");

        IOException e = assertThrows(IOException.class, () -> api.findItemName(999));
        assertTrue(e.getMessage().contains("status=404"));
    }

    @Test
    void 注文はPOSTで本文とContentTypeを送る() throws Exception {
        server.respond("/orders", 201, "order-1");

        assertEquals("order-1", api.createOrder(42, 2));
        assertEquals("POST", server.lastMethod());
        assertEquals("itemId=42&quantity=2", server.lastBody());
        assertEquals("application/x-www-form-urlencoded", server.lastHeader("Content-Type"));
    }

    @Test
    void 応答が遅いとタイムアウトする() {
        server.respond("/items/42", 200, "keyboard", Duration.ofSeconds(2));

        long start = System.nanoTime();
        assertThrows(HttpTimeoutException.class, () -> api.findItemName(42));
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(elapsedMillis < 1500, "タイムアウトまでに " + elapsedMillis + " ms かかった");
    }

    @Test
    void サーバーが止まっていると通信の例外になる() {
        server.close();

        assertThrows(IOException.class, () -> api.findItemName(42));
    }
}
