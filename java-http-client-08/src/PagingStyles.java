// 実行: java PagingStyles.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagingStyles {
    static final int TOTAL = 25; // サーバーが持つ商品は item-01 〜 item-25

    static Map<String, String> query(HttpExchange exchange) {
        Map<String, String> params = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw != null) {
            for (String pair : raw.split("&")) {
                String[] kv = pair.split("=", 2);
                params.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    // from(0始まり)から count 件を、1行1件の本文にして返す
    static void respond(HttpExchange exchange, int from, int count, String headerName, String headerValue) throws IOException {
        StringBuilder text = new StringBuilder();
        for (int i = from; i < Math.min(from + count, TOTAL); i++) {
            text.append(String.format("item-%02d%n", i + 1));
        }
        byte[] body = text.toString().getBytes(StandardCharsets.UTF_8);
        if (headerName != null) {
            exchange.getResponseHeaders().set(headerName, headerValue);
        }
        exchange.sendResponseHeaders(200, body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }

    static List<String> lines(HttpResponse<String> response) {
        return response.body().isBlank() ? List.of() : List.of(response.body().strip().split("\n"));
    }

    static HttpResponse<String> get(HttpClient client, String url) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // 方式A:ページ番号(1始まり)。全ページ数をヘッダーで教えてくれる
        server.createContext("/page", exchange -> {
            Map<String, String> q = query(exchange);
            int page = Integer.parseInt(q.get("page"));
            int size = Integer.parseInt(q.get("size"));
            respond(exchange, (page - 1) * size, size, "X-Total-Pages", String.valueOf((TOTAL + size - 1) / size));
        });
        // 方式B:オフセットと件数。全体の数は教えてくれない
        server.createContext("/offset", exchange -> {
            Map<String, String> q = query(exchange);
            respond(exchange, Integer.parseInt(q.get("offset")), Integer.parseInt(q.get("limit")), null, null);
        });
        // 方式C:カーソル。次に読む位置を表す文字列を返し、最後のページでは返さない
        server.createContext("/cursor", exchange -> {
            Map<String, String> q = query(exchange);
            int from = q.containsKey("cursor") ? Integer.parseInt(q.get("cursor").substring("after-".length())) : 0;
            int next = from + 10;
            respond(exchange, from, 10, next < TOTAL ? "X-Next-Cursor" : null, next < TOTAL ? "after-" + next : null);
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            // A:X-Total-Pages の回数だけ読む
            List<String> a = new ArrayList<>();
            int requests = 0;
            int totalPages = Integer.MAX_VALUE;
            for (int page = 1; page <= totalPages; page++) {
                HttpResponse<String> r = get(client, base + "/page?page=" + page + "&size=10");
                requests++;
                totalPages = Integer.parseInt(r.headers().firstValue("X-Total-Pages").orElseThrow());
                a.addAll(lines(r));
            }
            System.out.println("ページ番号 : " + a.size() + " 件, 要求 " + requests + " 回 (全ページ数 " + totalPages + " を見て終わる)");

            // B:件数が limit に満たないページが来たら終わる
            List<String> b = new ArrayList<>();
            requests = 0;
            for (int offset = 0; ; offset += 10) {
                List<String> items = lines(get(client, base + "/offset?offset=" + offset + "&limit=10"));
                requests++;
                b.addAll(items);
                if (items.size() < 10) {
                    break;
                }
            }
            System.out.println("オフセット : " + b.size() + " 件, 要求 " + requests + " 回 (limit 未満のページで終わる)");

            // C:X-Next-Cursor がなくなったら終わる
            List<String> c = new ArrayList<>();
            requests = 0;
            String cursor = null;
            do {
                HttpResponse<String> r = get(client, base + "/cursor" + (cursor == null ? "" : "?cursor=" + cursor));
                requests++;
                c.addAll(lines(r));
                cursor = r.headers().firstValue("X-Next-Cursor").orElse(null);
            } while (cursor != null);
            System.out.println("カーソル   : " + c.size() + " 件, 要求 " + requests + " 回 (次のカーソルがなくなると終わる)");

            // 3方式とも、同じ25件が読めたか
            System.out.println("3方式の結果が同じか: " + (a.equals(b) && b.equals(c)));
        } finally {
            server.stop(0);
        }
    }
}
