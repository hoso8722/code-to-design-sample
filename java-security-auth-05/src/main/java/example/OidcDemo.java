// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.OidcDemo
package example;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@SpringBootApplication
public class OidcDemo {
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

    static String json(String body, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"([^\"]+)\"").matcher(body);
        return m.find() ? m.group(1) : null;
    }

    // 認可コードフローを最後まで行い、トークンエンドポイントの応答(JSON)を返す
    static String login(String nonce) throws Exception {
        HttpClient browser = HttpClient.newBuilder().cookieHandler(new CookieManager()).followRedirects(HttpClient.Redirect.NEVER).build();
        String verifier = random();
        String challenge = B64.encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        Map<String, String> params = new LinkedHashMap<>();
        params.put("response_type", "code");
        params.put("client_id", "web-app");
        params.put("redirect_uri", REDIRECT);
        params.put("scope", "openid profile");
        params.put("state", random());
        params.put("nonce", nonce);
        params.put("code_challenge", challenge);
        params.put("code_challenge_method", "S256");
        HttpResponse<String> r = browser.send(HttpRequest.newBuilder(URI.create(base + "/oauth2/authorize?" + form(params))).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> page = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        Matcher csrf = Pattern.compile("name=\"_csrf\" type=\"hidden\" value=\"([^\"]+)\"").matcher(page.body());
        csrf.find();
        HttpResponse<String> done = browser.send(HttpRequest.newBuilder(URI.create(base + "/login")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form(Map.of("username", "alice", "password", "alice-pw", "_csrf", csrf.group(1))))).build(), HttpResponse.BodyHandlers.ofString());
        r = browser.send(HttpRequest.newBuilder(URI.create(done.headers().firstValue("Location").orElseThrow())).header("Accept", "text/html").build(), HttpResponse.BodyHandlers.ofString());
        Matcher code = Pattern.compile("[?&]code=([^&]*)").matcher(r.headers().firstValue("Location").orElseThrow());
        code.find();
        String body = form(Map.of("grant_type", "authorization_code", "code", URLDecoder.decode(code.group(1), StandardCharsets.UTF_8),
                "redirect_uri", REDIRECT, "code_verifier", verifier));
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(base + "/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("web-app:web-secret".getBytes(StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    // IDトークンの検証: 署名(JWK Set)と期限は NimbusJwtDecoder、iss、aud、nonce は自分で確かめる
    static String verifyIdToken(String idToken, String expectedNonce) {
        try {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(base + "/oauth2/jwks").build();
            Jwt jwt = decoder.decode(idToken);
            if (!base.equals(jwt.getIssuer().toString())) return "拒否: iss が違う";
            if (!jwt.getAudience().contains("web-app")) return "拒否: aud に自分(web-app)がない";
            if (!expectedNonce.equals(jwt.getClaimAsString("nonce"))) return "拒否: nonce が送ったものと違う";
            return "受理: sub=" + jwt.getSubject();
        } catch (Exception e) {
            return "拒否: " + e.getMessage();
        }
    }

    static String mask(String s) {
        return s.replaceAll("localhost:[0-9]+", "localhost:<ポート>").replaceAll("\"(exp|iat|nbf|auth_time)\":[0-9.]+", "\"$1\":…")
                .replaceAll("\"(jti|sid|at_hash)\":\"[^\"]+\"", "\"$1\":\"…\"").replaceAll("\"nonce\":\"[^\"]+\"", "\"nonce\":\"<nonce>\"");
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext context = SpringApplication.run(OidcDemo.class, "--server.port=0", "--spring.main.banner-mode=off", "--logging.level.root=WARN")) {
            base = "http://localhost:" + context.getEnvironment().getProperty("local.server.port");
            HttpClient plain = HttpClient.newHttpClient();

            System.out.println("## 1. ディスカバリー(/.well-known/openid-configuration)");
            String discovery = plain.send(HttpRequest.newBuilder(URI.create(base + "/.well-known/openid-configuration")).build(), HttpResponse.BodyHandlers.ofString()).body();
            for (String key : new String[] {"issuer", "authorization_endpoint", "token_endpoint", "jwks_uri", "userinfo_endpoint"}) {
                System.out.println("  " + key + ": " + mask(json(discovery, key)));
            }

            System.out.println("## 2. scope=openid profile で認可コードフロー");
            String nonce = random();
            String response = login(nonce);
            String idToken = json(response, "id_token");
            String accessToken = json(response, "access_token");
            System.out.println("  応答のキー: " + response.replaceAll("\"([a-z_]+)\":(\"[^\"]*\"|[0-9]+)", "$1 ").replaceAll("[{},]", "").trim());
            SignedJWT id = SignedJWT.parse(idToken);
            SignedJWT access = SignedJWT.parse(accessToken);
            System.out.println("  IDトークンのペイロード      : " + mask(id.getPayload().toString()));
            System.out.println("  アクセストークンのペイロード: " + mask(access.getPayload().toString()));

            System.out.println("## 3. IDトークンの検証");
            System.out.println("  正しい nonce          : " + verifyIdToken(idToken, nonce));
            System.out.println("  別の nonce を期待     : " + verifyIdToken(idToken, random()));
            String other = json(login(random()), "id_token");
            System.out.println("  別のログインのIDトークン(リプレイ): " + verifyIdToken(other, nonce));
            System.out.println("  アクセストークンをIDトークンとして: " + verifyIdToken(accessToken, nonce));

            System.out.println("## 4. UserInfoエンドポイント");
            HttpResponse<String> info = plain.send(HttpRequest.newBuilder(URI.create(base + "/userinfo")).header("Authorization", "Bearer " + accessToken).build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("  アクセストークン付き: " + info.statusCode() + " " + info.body());
            System.out.println("  IDトークンを渡した場合: " + plain.send(HttpRequest.newBuilder(URI.create(base + "/userinfo")).header("Authorization", "Bearer " + idToken).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        }
    }
}
