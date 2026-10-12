// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.AuthorizationDemo
package example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AuthorizationDemo {
    static HttpClient client = HttpClient.newHttpClient();
    static int port;

    static String call(String method, String path, String user, String password) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (user != null) {
            String credentials = Base64.getEncoder().encodeToString((user + ":" + password).getBytes());
            builder.header("Authorization", "Basic " + credentials);
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        String body = response.body().isBlank() ? "" : "  " + response.body();
        return response.statusCode() + body;
    }

    static void row(String label, String method, String path, String user, String password) throws Exception {
        System.out.printf("  %-34s %s%n", label, call(method, path, user, password));
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = SpringApplication.run(AuthorizationDemo.class, "--server.port=0", "--spring.main.banner-mode=off", "--logging.level.root=WARN")) {
            port = Integer.parseInt(context.getEnvironment().getProperty("local.server.port"));

            System.out.println("## 認証(誰か)と、URL単位の認可");
            row("認証なしで /v1/orders/1", "GET", "/v1/orders/1", null, null);
            row("aliceが /v1/orders/1", "GET", "/v1/orders/1", "alice", "alice-pw");
            row("aliceが /admin/x (URL単位で制限)", "GET", "/admin/x", "alice", "alice-pw");

            System.out.println("## 他人のデータ(IDOR): aliceが、bobの注文(id=2)を読む");
            row("1. 持ち主を確かめない /v1", "GET", "/v1/orders/2", "alice", "alice-pw");
            row("2. 自前のチェック /v2", "GET", "/v2/orders/2", "alice", "alice-pw");
            row("3. @PostAuthorize /v3", "GET", "/v3/orders/2", "alice", "alice-pw");
            System.out.println("## 自分の注文(id=1)は、読める");
            row("2. 自前のチェック /v2", "GET", "/v2/orders/1", "alice", "alice-pw");
            row("3. @PostAuthorize /v3", "GET", "/v3/orders/1", "alice", "alice-pw");

            System.out.println("## @PreAuthorize(hasRole('ADMIN')) のメソッド");
            row("aliceが、直接呼ぶ", "POST", "/direct/purge", "alice", "alice-pw");
            row("adminが、直接呼ぶ", "POST", "/direct/purge", "admin", "admin-pw");
            row("aliceが、自己呼び出しの入口から", "POST", "/self/purge", "alice", "alice-pw");
            row("aliceが、別Beanを経由する入口から", "POST", "/other/purge", "alice", "alice-pw");
            row("adminが、別Beanを経由する入口から", "POST", "/other/purge", "admin", "admin-pw");
        }
    }
}
