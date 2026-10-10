// 実行: java BoundedReader.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class BoundedReader {
    record Summary(int pagesRead, int itemsRead, String stoppedBecause) {}

    // 1ページずつ読み、ページごとに handler へ渡す。全体をメモリに溜めない
    static Summary readPages(HttpClient client, String url, int maxPages, int maxItems, Consumer<String> handler) throws Exception {
        int items = 0;
        for (int page = 1; page <= maxPages; page++) {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create(url + "?page=" + page)).build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return new Summary(page - 1, items, "ページ" + page + "の取得に失敗(status=" + response.statusCode() + ")");
            }
            String body = response.body().strip();
            if (body.isEmpty()) {
                return new Summary(page - 1, items, "空のページに達した(正常終了)");
            }
            for (String line : body.split("\n")) {
                if (items >= maxItems) {
                    return new Summary(page, items, "結果数の上限(" + maxItems + "件)に達した");
                }
                handler.accept(line);
                items++;
            }
        }
        return new Summary(maxPages, items, "ページ数の上限(" + maxPages + "ページ)に達した");
    }

    static void page(com.sun.net.httpserver.HttpExchange exchange, int status, String text) throws java.io.IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            exchange.getResponseBody().write(body);
        }
        exchange.close();
    }

    static String pageOf(com.sun.net.httpserver.HttpExchange exchange) {
        return exchange.getRequestURI().getQuery().substring("page=".length());
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        // 正常:3ページ(5件ずつ)あり、4ページ目は空
        server.createContext("/normal", exchange -> {
            int p = Integer.parseInt(pageOf(exchange));
            page(exchange, 200, p <= 3 ? "a\nb\nc\nd\ne\n" : "");
        });
        // 壊れたAPI:いつまでも空にならず、同じページを返し続ける
        server.createContext("/endless", exchange -> page(exchange, 200, "a\nb\nc\nd\ne\n"));
        // 途中で失敗:3ページ目だけ503
        server.createContext("/flaky", exchange -> {
            int p = Integer.parseInt(pageOf(exchange));
            if (p == 3) {
                page(exchange, 503, "down");
            } else {
                page(exchange, 200, p <= 5 ? "a\nb\nc\nd\ne\n" : "");
            }
        });
        server.start();
        String base = "http://localhost:" + server.getAddress().getPort();

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (String path : new String[] {"/normal", "/endless", "/flaky"}) {
                int[] seen = {0};
                Summary summary = readPages(client, base + path, 50, 100, item -> seen[0]++);
                System.out.printf("%-8s ページ%d回, 件数%d, 終了の理由: %s%n",
                        path, summary.pagesRead(), summary.itemsRead(), summary.stoppedBecause());
            }
        } finally {
            server.stop(0);
        }
    }
}
