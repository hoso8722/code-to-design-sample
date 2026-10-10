package example.books;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

// アプリケーション全体を、ランダムなポートで起動し、本物のPostgreSQLにつないで確かめる。Dockerが必要
@Tag("integration")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIntegrationTest {
    // 固定のバージョンのPostgreSQLを起動し、アプリケーションの接続先にする(@ServiceConnection)
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @LocalServerPort
    int port;

    @Autowired
    JdbcClient jdbc;

    HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void resetData() {
        // テストごとに、表を初期状態に戻す
        jdbc.sql("TRUNCATE loans, books RESTART IDENTITY").update();
        jdbc.sql("INSERT INTO books (title, copies) VALUES ('吾輩は猫である', 3), ('坊っちゃん', 2)").update();
    }

    private HttpResponse<String> send(String method, String path, String user, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        if (user != null) {
            String credentials = user + ":" + (user.equals("admin") ? "admin-test-pass" : "reader-test-pass");
            builder.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private int copiesOf(long id) {
        return jdbc.sql("SELECT copies FROM books WHERE id = :id").param("id", id).query(Integer.class).single();
    }

    private int loanCount() {
        return jdbc.sql("SELECT count(*) FROM loans").query(Integer.class).single();
    }

    @Test
    void 保護されたURIは_認証なしでは401() throws Exception {
        assertEquals(401, send("GET", "/api/books", null, null).statusCode());
    }

    @Test
    void 登録した本は_データベースに保存され_同じ題名は409になる() throws Exception {
        assertEquals(201, send("POST", "/api/books", "admin", "{\"title\":\"こころ\",\"copies\":4}").statusCode());
        assertEquals(1, jdbc.sql("SELECT count(*) FROM books WHERE title = 'こころ'").query(Integer.class).single());

        assertEquals(409, send("POST", "/api/books", "admin", "{\"title\":\"こころ\",\"copies\":1}").statusCode());
    }

    @Test
    void 貸出に成功すると_在庫が減り_記録が残る() throws Exception {
        assertEquals(201, send("POST", "/api/books/1/loans", "admin", "{\"borrower\":\"Sora\"}").statusCode());

        assertEquals(2, copiesOf(1));
        assertEquals(1, loanCount());
    }

    @Test
    void 貸出の途中で失敗すると_在庫の減少もロールバックされる() throws Exception {
        assertEquals(500, send("POST", "/api/books/1/loans", "admin", "{\"borrower\":\"error\"}").statusCode());

        assertEquals(3, copiesOf(1));
        assertEquals(0, loanCount());
    }

    @Test
    void 検査例外でもrollbackForを指定した貸出は_ロールバックされる() throws Exception {
        assertEquals(500, send("POST", "/api/books/1/loans-checked-safe", "admin", "{\"borrower\":\"error\"}").statusCode());

        assertEquals(3, copiesOf(1));
        assertEquals(0, loanCount());
    }

    @Test
    void 冊数のCHECK制約は_データベースが守る() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.sql("UPDATE books SET copies = -1 WHERE id = 1").update());
    }
}
