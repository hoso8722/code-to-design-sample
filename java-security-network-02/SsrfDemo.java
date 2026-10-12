// 実行: java SsrfDemo.java
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class SsrfDemo {
    // 内部のアドレスか。ループバック、プライベート、リンクローカル、全アドレス、マルチキャストを禁止する
    static boolean isInternal(InetAddress address) {
        return address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress();
    }

    // JDKのメソッドが見逃す範囲を足す: IPv4の共有アドレス空間(100.64.0.0/10)と、IPv6のユニークローカル(fc00::/7)
    static boolean isInternalStrict(InetAddress address) {
        if (isInternal(address)) return true;
        byte[] b = address.getAddress();
        if (b.length == 4) return (b[0] & 0xff) == 100 && (b[1] & 0xc0) == 64;
        return (b[0] & 0xfe) == 0xfc;
    }

    // URLを検査する。スキームを絞り、名前解決の結果(複数ありうる)がすべて許されるアドレスかを確かめる
    static void validate(URI uri, Predicate<InetAddress> forbidden) throws Exception {
        if (!Set.of("http", "https").contains(String.valueOf(uri.getScheme()).toLowerCase())) {
            throw new SecurityException("許可しないスキーム: " + uri.getScheme());
        }
        if (uri.getHost() == null) throw new SecurityException("ホストがない: " + uri);
        for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
            if (forbidden.test(address)) throw new SecurityException("内部のアドレス: " + uri.getHost() + " -> " + address.getHostAddress());
        }
    }

    static final HttpClient FOLLOWING = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    static final HttpClient MANUAL = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    // 1. 検査なし: 利用者のURLをそのまま取得する
    static String fetchUnchecked(String url) throws Exception {
        return FOLLOWING.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    // 2. 最初のURLだけ検査して、リダイレクトは自動で辿る
    static String fetchFirstOnly(String url, Predicate<InetAddress> forbidden) throws Exception {
        validate(URI.create(url), forbidden);
        return FOLLOWING.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    // 3. リダイレクトを自分で辿り、転送先ごとに検査する
    static String fetchEachHop(String url, Predicate<InetAddress> forbidden) throws Exception {
        URI current = URI.create(url);
        for (int hop = 0; hop < 3; hop++) {
            validate(current, forbidden);
            HttpResponse<String> response = MANUAL.send(HttpRequest.newBuilder(current).build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 3) return response.body();
            current = current.resolve(response.headers().firstValue("Location").orElseThrow());
        }
        throw new SecurityException("リダイレクトが多すぎる");
    }

    static HttpServer start(String host, String path, int status, String body, String location) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(host, 0), 0);
        server.createContext(path, exchange -> {
            if (location != null) exchange.getResponseHeaders().add("Location", location);
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, status == 302 ? -1 : bytes.length);
            if (status != 302) exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return server;
    }

    interface Action { String run() throws Exception; }

    static void show(String label, Action action) {
        try {
            System.out.printf("  %-30s 取得 %s%n", label, action.run());
        } catch (Exception e) {
            System.out.printf("  %-30s 拒否 %s: %s%n", label, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. アドレスの分類");
        for (String host : List.of("localhost", "127.0.0.1", "10.1.2.3", "172.16.0.1", "192.168.0.1", "169.254.169.254",
                "0.0.0.0", "[::1]", "2130706433", "[::ffff:127.0.0.1]", "[fd00::1]", "100.64.0.1", "93.184.216.34")) {
            InetAddress address = InetAddress.getByName(host.replaceAll("^\\[|\\]$", ""));
            System.out.printf("  %-22s -> %-20s JDKのメソッドのみ:%s  追加の検査あり:%s%n", host, address.getHostAddress(),
                    isInternal(address) ? "内部" : "外部", isInternalStrict(address) ? "内部" : "外部");
        }

        // 内部のサービス(127.0.0.1)と、公開のサーバー役(127.0.0.2)を立てる
        HttpServer internal = start("127.0.0.1", "/", 200, "内部の管理用トークン: internal-token", null);
        int internalPort = internal.getAddress().getPort();
        String internalUrl = "http://127.0.0.1:" + internalPort + "/";
        HttpServer publicServer = start("127.0.0.2", "/", 200, "公開のページ", null);
        String publicUrl = "http://127.0.0.2:" + publicServer.getAddress().getPort() + "/";
        HttpServer redirector = start("127.0.0.2", "/", 302, "", internalUrl);
        String redirectUrl = "http://127.0.0.2:" + redirector.getAddress().getPort() + "/";

        // 実験用の方針: 内部のサービスのアドレス(127.0.0.1)だけを禁止する(公開のサーバー役を127.0.0.2に置いたため)
        Predicate<InetAddress> forbidden = address -> address.getHostAddress().equals("127.0.0.1");
        try {
            System.out.println("## 2. 検査なし: 利用者のURLをそのまま取得");
            show("内部のURL", () -> fetchUnchecked(internalUrl));

            System.out.println("## 3. スキームとアドレスの検査");
            show("内部のURL", () -> fetchFirstOnly(internalUrl, forbidden));
            show("file スキーム", () -> fetchFirstOnly("file:///etc/hostname", forbidden));
            show("公開のURL", () -> fetchFirstOnly(publicUrl, forbidden));

            System.out.println("## 4. リダイレクト(公開のURLが、内部のURLへ転送する)");
            show("最初のURLだけ検査", () -> fetchFirstOnly(redirectUrl, forbidden));
            show("転送先ごとに検査", () -> fetchEachHop(redirectUrl, forbidden));
            show("転送先ごとに検査(公開のURL)", () -> fetchEachHop(publicUrl, forbidden));
        } finally {
            internal.stop(0);
            publicServer.stop(0);
            redirector.stop(0);
        }
    }
}
