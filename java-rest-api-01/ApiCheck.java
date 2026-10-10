// 実行: java ApiCheck.java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// 起動中のAPIへ、JDKのHttpClientでGETを送って、ステータスとヘッダー、本文を表示する
public class ApiCheck {
    public static void main(String[] args) throws Exception {
        String port = args.length > 0 ? args[0] : System.getenv("API_PORT");
        if (port == null) {
            System.out.println("使い方: java ApiCheck.java <ポート番号>  (または環境変数 API_PORT)");
            return;
        }
        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/api/books", "/api/books/1"}) {
                HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .header("Accept", "application/json")
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("GET " + path + " -> " + response.statusCode()
                        + " " + response.headers().firstValue("Content-Type").orElse("(なし)"));
                System.out.println("  " + response.body());
            }
        }
    }
}
