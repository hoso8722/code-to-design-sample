// 実行: java TokenLeakDemo.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

public class TokenLeakDemo {
    static final String TOKEN = "eyJhbGciOiJSUzI1NiJ9.demo-payload.demo-signature"; // 実験用の仮のトークン
    static final Pattern SECRET_VALUE = Pattern.compile("(?i)(access_token=|bearer\\s+)[A-Za-z0-9._~+/=-]+");

    // ログに出す前に、トークンらしい値を伏せる
    static String mask(String line) {
        return SECRET_VALUE.matcher(line).replaceAll("$1****");
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            // リクエストの1行目(メソッドとURI)を、そのまま記録するアクセスログ
            String requestLine = exchange.getRequestMethod() + " " + exchange.getRequestURI();
            System.out.println("  アクセスログ(そのまま)   : " + requestLine);
            System.out.println("  アクセスログ(伏せたあと) : " + mask(requestLine));
            // 全ヘッダーを記録するデバッグ用のログ
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (authorization != null) {
                System.out.println("  全ヘッダーのログ(そのまま)  : Authorization: " + authorization);
                System.out.println("  全ヘッダーのログ(伏せたあと): Authorization: " + mask(authorization));
            }
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();
        HttpClient client = HttpClient.newHttpClient();
        try {
            System.out.println("## 1. トークンを、URLのクエリに付けた場合");
            client.send(HttpRequest.newBuilder(URI.create(base + "/api/orders?access_token=" + TOKEN)).build(), HttpResponse.BodyHandlers.discarding());
            System.out.println("## 2. トークンを、Authorizationヘッダーに付けた場合");
            client.send(HttpRequest.newBuilder(URI.create(base + "/api/orders")).header("Authorization", "Bearer " + TOKEN).build(), HttpResponse.BodyHandlers.discarding());
        } finally {
            server.stop(0);
        }
    }
}
