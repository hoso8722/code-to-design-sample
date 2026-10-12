// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.RedirectUriDemo
package example;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RedirectUriDemo {
    static final String REGISTERED = "https://client.example/callback";

    static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    static String form(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> sb.append(sb.isEmpty() ? "" : "&").append(enc(k)).append("=").append(enc(v)));
        return sb.toString();
    }

    // ===== 甘い検証(よくない例): 登録された値で始まっていれば許す =====
    static boolean naiveMatch(String requested) {
        return requested.startsWith(REGISTERED);
    }

    // ===== 厳格な検証: 登録された値と、完全に一致する場合だけ許す =====
    static boolean exactMatch(String requested) {
        return REGISTERED.equals(requested);
    }

    // ログインした状態のブラウザの代わり
    static HttpClient loggedInBrowser(String base) throws Exception {
        HttpClient browser = HttpClient.newBuilder().cookieHandler(new CookieManager()).followRedirects(HttpClient.Redirect.NEVER).build();
        HttpResponse<String> page = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        Matcher csrf = Pattern.compile("name=\"_csrf\" type=\"hidden\" value=\"([^\"]+)\"").matcher(page.body());
        csrf.find();
        browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(Map.of("username", "alice", "password", "alice-pw", "_csrf", csrf.group(1))))).build(), HttpResponse.BodyHandlers.ofString());
        return browser;
    }

    static String authorize(HttpClient browser, String base, String clientId, String redirectUri) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUri);
        params.put("scope", "read");
        params.put("state", "xyz");
        params.put("code_challenge", "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"); // RFC 7636 の例の値
        params.put("code_challenge_method", "S256");
        HttpResponse<String> r = browser.send(HttpRequest.newBuilder(URI.create(base + "/oauth2/authorize?" + form(params))).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        String location = r.headers().firstValue("Location").orElse("");
        if (r.statusCode() == 302 && location.contains("code=")) return "302 → " + location.replaceAll("code=[^&]+", "code=<認可コード>").replaceAll("state=[^&]+", "state=<state>");
        if (r.statusCode() == 302) return "302 → " + location;
        return r.statusCode() + " (リダイレクトしない)";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("## 1. 検証のしかたによる違い(登録された値: " + REGISTERED + ")");
        String[] candidates = {
                REGISTERED,
                REGISTERED + ".evil.example",
                REGISTERED + "/../evil",
                REGISTERED + "?next=https://evil.example",
                "https://client.example/callback#x",
                "https://client.example.evil.example/callback",
        };
        System.out.printf("  %-52s %-8s %s%n", "リクエストの redirect_uri", "前方一致", "完全一致");
        for (String c : candidates) System.out.printf("  %-52s %-8s %s%n", c, naiveMatch(c) ? "許す" : "拒否", exactMatch(c) ? "許す" : "拒否");

        try (ConfigurableApplicationContext context = SpringApplication.run(RedirectUriDemo.class, "--server.port=0", "--spring.main.banner-mode=off", "--logging.level.root=ERROR")) {
            String base = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            HttpClient browser = loggedInBrowser(base);

            System.out.println("## 2. Spring Authorization Server に、同じ redirect_uri を送る(web-app)");
            for (String c : candidates) System.out.printf("  %-52s %s%n", c, authorize(browser, base, "web-app", c));

            System.out.println("## 3. ループバックのアドレス(127.0.0.1)を登録したクライアント(local-app)");
            for (String c : new String[] {"http://127.0.0.1:9999/callback", "http://127.0.0.1:12345/callback", "http://127.0.0.1:9999/other", "http://localhost:9999/callback"})
                System.out.printf("  %-52s %s%n", c, authorize(browser, base, "local-app", c));
        }
    }
}
