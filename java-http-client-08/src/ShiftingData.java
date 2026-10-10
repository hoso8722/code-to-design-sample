// 実行: java ShiftingData.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShiftingData {
    static List<String> data;

    static void reset() {
        data = new CopyOnWriteArrayList<>();
        for (int i = 1; i <= 25; i++) {
            data.add(String.format("item-%02d", i));
        }
    }

    static void send(HttpExchange exchange, List<String> items, String nextCursor) throws IOException {
        byte[] body = String.join("\n", items).getBytes(StandardCharsets.UTF_8);
        if (nextCursor != null) {
            exchange.getResponseHeaders().set("X-Next-Cursor", nextCursor);
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
        // オフセット方式:先頭から数えた位置で切り出す
        server.createContext("/offset", exchange -> {
            int offset = Integer.parseInt(exchange.getRequestURI().getQuery().split("&")[0].substring("offset=".length()));
            int to = Math.min(offset + 10, data.size());
            send(exchange, offset >= data.size() ? List.of() : new ArrayList<>(data.subList(offset, to)), null);
        });
        // カーソル方式:「この項目の次から」と指定する
        server.createContext("/cursor", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            int from = query == null ? 0 : data.indexOf(query.substring("after=".length())) + 1;
            int to = Math.min(from + 10, data.size());
            send(exchange, new ArrayList<>(data.subList(from, to)), to < data.size() ? data.get(to - 1) : null);
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            // オフセット方式:1ページ目を読んだあと、先頭に新しい項目が1件追加される
            reset();
            List<String> offsetResult = new ArrayList<>();
            for (int offset = 0; ; offset += 10) {
                List<String> items = lines(get(client, base + "/offset?offset=" + offset));
                offsetResult.addAll(items);
                if (offset == 0) {
                    data.add(0, "item-00"); // 読んでいる途中でデータが増えた
                }
                if (items.size() < 10) {
                    break;
                }
            }
            report("オフセット", offsetResult);

            // カーソル方式:同じ状況
            reset();
            List<String> cursorResult = new ArrayList<>();
            String cursor = null;
            boolean inserted = false;
            do {
                HttpResponse<String> r = get(client, base + "/cursor" + (cursor == null ? "" : "?after=" + cursor));
                cursorResult.addAll(lines(r));
                cursor = r.headers().firstValue("X-Next-Cursor").orElse(null);
                if (!inserted) {
                    data.add(0, "item-00");
                    inserted = true;
                }
            } while (cursor != null);
            report("カーソル", cursorResult);
        } finally {
            server.stop(0);
        }
    }

    static void report(String label, List<String> result) {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        for (String item : result) {
            if (!seen.add(item)) {
                duplicates.add(item);
            }
        }
        List<String> sorted = new ArrayList<>(seen);
        Collections.sort(sorted);
        System.out.println(label + ": 読めた " + result.size() + " 件, 重複 " + duplicates + ", 異なる項目 " + seen.size() + " 件");
    }
}
