// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.AuthorizationCodeFlow
package example;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AuthorizationCodeFlow {
    static String base;
    static final String REDIRECT = "http://127.0.0.1:9999/callback";
    static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    static String form(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> sb.append(sb.isEmpty() ? "" : "&").append(enc(k)).append("=").append(enc(v)));
        return sb.toString();
    }

    static String random() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return B64.encodeToString(bytes);
    }

    static String challenge(String verifier) throws Exception {
        return B64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
    }

    // リダイレクトを自動で辿らないクライアント。Cookie(ログインのセッション)は保持する
    static HttpClient newBrowser() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).followRedirects(HttpClient.Redirect.NEVER).build();
    }

    static HttpResponse<String> send(HttpClient client, HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // 認可リクエストを送り、ログインして、リダイレクト先(redirect_uri)の Location を返す
    static String authorize(HttpClient browser, String clientId, Map<String, String> extra) throws Exception {
        Map<String, String> params = new java.util.LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("redirect_uri", REDIRECT);
        params.put("scope", "read");
        params.putAll(extra);
        HttpResponse<String> r = send(browser, HttpRequest.newBuilder(URI.create(base + "/oauth2/authorize?" + form(params))).header("Accept", "text/html").build());
        if (r.statusCode() == 302 && r.headers().firstValue("Location").orElse("").contains("/login")) {
            // ログインしていないので、ログイン画面に飛ばされた。CSRFトークンを取り出して、ログインする
            HttpResponse<String> login = send(browser, HttpRequest.newBuilder(URI.create(base + "/login")).header("Accept", "text/html").build());
            Matcher m = Pattern.compile("name=\"_csrf\" type=\"hidden\" value=\"([^\"]+)\"").matcher(login.body());
            m.find();
            String post = form(Map.of("username", "alice", "password", "alice-pw", "_csrf", m.group(1)));
            HttpResponse<String> done = send(browser, HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(post)).build());
            r = send(browser, HttpRequest.newBuilder(URI.create(done.headers().firstValue("Location").orElseThrow())).build());
        }
        return r.headers().firstValue("Location").orElse("(Locationなし) status=" + r.statusCode());
    }

    static String param(String location, String name) {
        Matcher m = Pattern.compile("[?&]" + name + "=([^&]*)").matcher(location);
        return m.find() ? java.net.URLDecoder.decode(m.group(1), StandardCharsets.UTF_8) : null;
    }

    static String lastBody;

    static String token(HttpClient client, String clientId, String secret, Map<String, String> params) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + "/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded");
        if (secret != null) b.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + secret).getBytes(StandardCharsets.UTF_8)));
        else params = withClientId(params, clientId);
        HttpResponse<String> r = send(client, b.POST(HttpRequest.BodyPublishers.ofString(form(params))).build());
        lastBody = r.body();
        return r.statusCode() + " " + shorten(r.body());
    }

    static Map<String, String> withClientId(Map<String, String> params, String clientId) {
        Map<String, String> m = new java.util.LinkedHashMap<>(params);
        m.put("client_id", clientId);
        return m;
    }

    // トークンの長い値は、先頭だけ表示する
    static String shorten(String body) {
        return body.replaceAll("\"(access_token|id_token|refresh_token)\":\"([^\"]{12})[^\"]*\"", "\"$1\":\"$2…\"");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = SpringApplication.run(AuthorizationCodeFlow.class, "--server.port=0", "--spring.main.banner-mode=off", "--logging.level.root=WARN")) {
            base = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            HttpClient plain = HttpClient.newHttpClient();

            System.out.println("## 1. 認可コードフロー(web-app: 秘密を持つクライアント + PKCE)");
            String verifier = random();
            String state = random();
            String location = authorize(newBrowser(), "web-app", Map.of("state", state, "code_challenge", challenge(verifier), "code_challenge_method", "S256"));
            System.out.println("  認可サーバーの応答(Location): " + location.replaceAll("code=[^&]+", "code=<認可コード>").replaceAll("state=[^&]+", "state=<state>"));
            System.out.println("  state が一致: " + state.equals(param(location, "state")));
            String code = param(location, "code");
            System.out.println("  トークンの取得: " + token(plain, "web-app", "web-secret",
                    Map.of("grant_type", "authorization_code", "code", code, "redirect_uri", REDIRECT, "code_verifier", verifier)));

            // アクセストークンはJWT。中身を読む
            String accessToken = param("?" + "x=" + lastBody.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1"), "x");
            String[] jwt = accessToken.split("\\.");
            System.out.println("  アクセストークンのヘッダー  : " + new String(Base64.getUrlDecoder().decode(jwt[0]), StandardCharsets.UTF_8).replaceAll("\"kid\":\"[^\"]+\"", "\"kid\":\"…\""));
            System.out.println("  アクセストークンのペイロード: " + new String(Base64.getUrlDecoder().decode(jwt[1]), StandardCharsets.UTF_8).replaceAll("\"(exp|iat|nbf)\":[0-9.]+", "\"$1\":…").replaceAll("\"jti\":\"[^\"]+\"", "\"jti\":\"…\"").replaceAll("localhost:[0-9]+", "localhost:<ポート>"));

            System.out.println("## 2. 認可コードは1回しか使えない");
            System.out.println("  同じコードをもう一度: " + token(plain, "web-app", "web-secret",
                    Map.of("grant_type", "authorization_code", "code", code, "redirect_uri", REDIRECT, "code_verifier", verifier)));

            System.out.println("## 3. PKCEを必須にしたクライアントで、code_challenge を付けない認可リクエスト");
            System.out.println("  " + authorize(newBrowser(), "web-app", Map.of("state", state)).replaceAll("state=[^&]+", "state=<state>"));

            System.out.println("## 4. 認可コードが盗まれた場合(mobile-app: 秘密を持たないクライアント)");
            String v2 = random();
            String loc2 = authorize(newBrowser(), "mobile-app", Map.of("state", state, "code_challenge", challenge(v2), "code_challenge_method", "S256"));
            String stolen = param(loc2, "code");
            System.out.println("  攻撃者が code_verifier なしで交換: " + token(plain, "mobile-app", null,
                    Map.of("grant_type", "authorization_code", "code", stolen, "redirect_uri", REDIRECT)));
            System.out.println("  攻撃者が別の code_verifier で交換 : " + token(plain, "mobile-app", null,
                    Map.of("grant_type", "authorization_code", "code", stolen, "redirect_uri", REDIRECT, "code_verifier", random())));
            System.out.println("  正規のクライアントが交換          : " + token(plain, "mobile-app", null,
                    Map.of("grant_type", "authorization_code", "code", stolen, "redirect_uri", REDIRECT, "code_verifier", v2)));
        }
    }
}
