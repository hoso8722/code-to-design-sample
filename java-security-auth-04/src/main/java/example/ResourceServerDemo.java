// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ResourceServerDemo
package example;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import example.authserver.AuthServerApp;
import example.resource.ResourceApp;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public class ResourceServerDemo {
    static final HttpClient CLIENT = HttpClient.newHttpClient();
    static String authBase;
    static String apiBase;

    static String tokenFor(String clientId, String secret, String scope) throws Exception {
        String body = "grant_type=client_credentials&scope=" + scope;
        HttpResponse<String> r = CLIENT.send(HttpRequest.newBuilder(URI.create(authBase + "/oauth2/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + secret).getBytes(StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        Matcher m = Pattern.compile("\"access_token\":\"([^\"]+)\"").matcher(r.body());
        m.find();
        return m.group(1);
    }

    static void call(String label, String method, String path, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(apiBase + path)).method(method, HttpRequest.BodyPublishers.noBody());
        if (token != null) b.header("Authorization", "Bearer " + token);
        HttpResponse<String> r = CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
        String header = r.headers().firstValue("WWW-Authenticate").orElse("");
        Matcher e = Pattern.compile("error=\"([^\"]*)\"(?:, error_description=\"([^\"]*)\")?").matcher(header);
        String www = e.find() ? "  [" + e.group(1) + (e.group(2) != null ? ": " + e.group(2).replace("An error occurred while attempting to decode the Jwt: ", "").replaceAll("expired at [0-9T:Z-]+", "expired at <時刻>") : "") + "]" : "";
        System.out.printf("  %-34s %d %s%s%n", label, r.statusCode(), r.body().length() > 60 ? "" : r.body(), www);
    }

    static String part(String token, int index) {
        return token.split("\\.")[index];
    }

    static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext auth = new SpringApplicationBuilder(AuthServerApp.class)
                .profiles("authserver").properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=WARN").run();
             ) {
            authBase = "http://localhost:" + auth.getEnvironment().getProperty("local.server.port");
            try (ConfigurableApplicationContext api = new SpringApplicationBuilder(ResourceApp.class)
                    .properties("server.port=0", "spring.main.banner-mode=off", "logging.level.root=WARN", "app.issuer=" + authBase).run()) {
                apiBase = "http://localhost:" + api.getEnvironment().getProperty("local.server.port");

                String readOnly = tokenFor("orders-client", "orders-secret", "read");
                String readWrite = tokenFor("orders-client", "orders-secret", "read%20write");

                System.out.println("## 1. 正しいトークン");
                call("トークンなし GET /api/orders", "GET", "/api/orders", null);
                call("read    GET  /api/orders", "GET", "/api/orders", readOnly);
                call("read    POST /api/orders", "POST", "/api/orders", readOnly);
                call("read+write POST /api/orders", "POST", "/api/orders", readWrite);
                call("read    POST /api/orders/cancel", "POST", "/api/orders/cancel", readOnly);
                call("read+write POST /api/orders/cancel", "POST", "/api/orders/cancel", readWrite);

                System.out.println("## 2. 細工したトークン(すべて read のトークンをもとにしている)");
                String tampered = part(readOnly, 0) + "." + b64(new String(Base64.getUrlDecoder().decode(part(readOnly, 1)), StandardCharsets.UTF_8)
                        .replace("\"scope\":[\"read\"]", "\"scope\":[\"read\",\"write\"]")) + "." + part(readOnly, 2);
                call("scope を書き換えた", "POST", "/api/orders", tampered);
                String noneToken = b64("{\"alg\":\"none\"}") + "." + part(readOnly, 1) + ".";
                call("alg=none", "GET", "/api/orders", noneToken);
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair rogue = generator.generateKeyPair();
                SignedJWT forged = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(SignedJWT.parse(readOnly).getHeader().getKeyID()).build(),
                        new JWTClaimsSet.Builder(SignedJWT.parse(readOnly).getJWTClaimsSet()).claim("scope", "read write").build());
                forged.sign(new RSASSASigner((RSAPrivateKey) rogue.getPrivate()));
                call("別の鍵で署名(kidは同じ)", "POST", "/api/orders", forged.serialize());
                call("別のAPI(billing-api)向け", "GET", "/api/orders", tokenFor("billing-client", "billing-secret", "read"));

                System.out.println("## 3. 有効期限(有効期間2秒のトークンを、3秒後に使う)");
                String shortLived = tokenFor("short-client", "short-secret", "read");
                call("2秒以内", "GET", "/api/orders", shortLived);
                Thread.sleep(3000);
                call("3秒後(許容する時計のずれ 0秒)", "GET", "/api/orders", shortLived);
                JwtDecoder defaultSkew = NimbusJwtDecoder.withJwkSetUri(authBase + "/oauth2/jwks").build();
                ((NimbusJwtDecoder) defaultSkew).setJwtValidator(JwtValidators.createDefaultWithIssuer(authBase));
                try {
                    defaultSkew.decode(shortLived);
                    System.out.println("  3秒後(JwtValidators の既定の設定): 受理された");
                } catch (Exception e) {
                    System.out.println("  3秒後(JwtValidators の既定の設定): 拒否 " + e.getMessage());
                }
            }
        }
    }
}
