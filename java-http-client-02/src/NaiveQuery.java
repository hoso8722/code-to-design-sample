// 実行: java NaiveQuery.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class NaiveQuery {
    // 受け取ったクエリを「&」で分け、「=」でキーと値に分けて返すサーバー
    static Map<String, String> parse(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return params;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/search", exchange -> {
            String rawQuery = exchange.getRequestURI().getRawQuery();
            byte[] body = ("raw=" + rawQuery + "\nparams=" + parse(rawQuery)).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        String userInput = "tea&admin=true";
        try (HttpClient client = HttpClient.newHttpClient()) {
            String naive = base + "/search?q=" + userInput;
            String encoded = base + "/search?q=" + URLEncoder.encode(userInput, StandardCharsets.UTF_8);
            for (String uri : new String[] {naive, encoded}) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("送ったURI: " + uri.substring(base.length()));
                System.out.println(response.body());
                System.out.println();
            }
        }
        server.stop(0);
    }
}
