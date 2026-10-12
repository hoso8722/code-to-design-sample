// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.RefreshDemo
package example;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@SpringBootApplication
public class RefreshDemo {
    static String base;
    static final String REDIRECT = "http://127.0.0.1:9999/callback";
    static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    // refresh_token の扱いだけが違う、2つのクライアントを登録する
    @Bean
    RegisteredClientRepository clients() {
        return new InMemoryRegisteredClientRepository(client("reuse-client", true), client("rotate-client", false));
    }

    static RegisteredClient client(String id, boolean reuseRefreshTokens) {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(id).clientSecret("{noop}" + id + "-secret")   // 実験用の仮の値
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(REDIRECT).scope("read")
                .clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
                .tokenSettings(TokenSettings.builder().reuseRefreshTokens(reuseRefreshTokens).build())
                .build();
    }

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

    static String json(String body, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"?([^\",}]+)").matcher(body);
        return m.find() ? m.group(1) : null;
    }

    static String post(String path, String clientId, Map<String, String> params) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientId + "-secret").getBytes(StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.ofString(form(params))).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    // 認可コードフローを最後まで行い、トークンエンドポイントの応答(JSON)を返す
    static String login(String clientId) throws Exception {
        HttpClient browser = HttpClient.newBuilder().cookieHandler(new CookieManager()).followRedirects(HttpClient.Redirect.NEVER).build();
        String verifier = random();
        String challenge = B64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", clientId);
        params.put("redirect_uri", REDIRECT);
        params.put("scope", "read");
        params.put("state", random());
        params.put("code_challenge", challenge);
        params.put("code_challenge_method", "S256");
        browser.send(HttpRequest.newBuilder(URI.create(base + "/oauth2/authorize?" + form(params))).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> page = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        Matcher csrf = Pattern.compile("name=\"_csrf\" type=\"hidden\" value=\"([^\"]+)\"").matcher(page.body());
        csrf.find();
        HttpResponse<String> done = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(Map.of("username", "alice", "password", "alice-pw", "_csrf", csrf.group(1))))).build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> r = browser.send(HttpRequest.newBuilder(URI.create(done.headers().firstValue("Location").orElseThrow())).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        Matcher code = Pattern.compile("[?&]code=([^&]*)").matcher(r.headers().firstValue("Location").orElseThrow());
        code.find();
        return post("/oauth2/token", clientId, Map.of("grant_type", "authorization_code", "code", URLDecoder.decode(code.group(1), StandardCharsets.UTF_8),
                "redirect_uri", REDIRECT, "code_verifier", verifier));
    }

    static String refresh(String clientId, String refreshToken) throws Exception {
        return post("/oauth2/token", clientId, Map.of("grant_type", "refresh_token", "refresh_token", refreshToken));
    }

    static String result(String body) {
        return body.contains("error") ? "拒否 " + body : "成功(アクセストークン " + json(body, "access_token").substring(0, 8) + "…, "
                + (json(body, "refresh_token") != null ? "リフレッシュトークン " + json(body, "refresh_token").substring(0, 8) + "…" : "リフレッシュトークンなし") + ")";
    }

    static boolean active(String clientId, String token) throws Exception {
        return post("/oauth2/introspect", clientId, Map.of("token", token)).contains("\"active\":true");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = SpringApplication.run(RefreshDemo.class, "--server.port=0", "--spring.main.banner-mode=off", "--logging.level.root=WARN")) {
            base = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");

            TokenSettings defaults = TokenSettings.builder().build();
            System.out.println("## 1. 既定の有効期間");
            System.out.println("  アクセストークン      : " + defaults.getAccessTokenTimeToLive());
            System.out.println("  リフレッシュトークン  : " + defaults.getRefreshTokenTimeToLive());
            System.out.println("  認可コード            : " + defaults.getAuthorizationCodeTimeToLive());
            System.out.println("  リフレッシュトークンの使い回し(既定): " + defaults.isReuseRefreshTokens());

            for (String clientId : new String[] {"reuse-client", "rotate-client"}) {
                System.out.println("## 2. " + clientId + (clientId.startsWith("reuse") ? "(リフレッシュトークンを使い回す)" : "(リフレッシュトークンをローテーションする)"));
                String first = login(clientId);
                String refresh1 = json(first, "refresh_token");
                System.out.println("  最初の取得                  : " + result(first));
                String second = refresh(clientId, refresh1);
                String refresh2 = json(second, "refresh_token");
                System.out.println("  refresh1 で更新             : " + result(second) + (refresh1.equals(refresh2) ? " [同じ値]" : " [新しい値]"));
                System.out.println("  refresh1 をもう一度使う     : " + result(refresh(clientId, refresh1)));
                System.out.println("  refresh2 を使う             : " + result(refresh(clientId, refresh2)));
            }

            System.out.println("## 3. 失効(revocation)");
            String tokens = login("rotate-client");
            String access = json(tokens, "access_token");
            String refreshToken = json(tokens, "refresh_token");
            System.out.println("  失効前: introspection で active        : " + active("rotate-client", access));
            post("/oauth2/revoke", "rotate-client", Map.of("token", refreshToken, "token_type_hint", "refresh_token"));
            System.out.println("  リフレッシュトークンを失効した後");
            System.out.println("    リフレッシュトークンで更新           : " + result(refresh("rotate-client", refreshToken)));
            System.out.println("    アクセストークンの introspection     : active=" + active("rotate-client", access));
            try {
                NimbusJwtDecoder.withJwkSetUri(base + "/oauth2/jwks").build().decode(access);
                System.out.println("    アクセストークンの署名と期限だけを検証: 受理された");
            } catch (Exception e) {
                System.out.println("    アクセストークンの署名と期限だけを検証: 拒否 " + e.getMessage());
            }
        }
    }
}
