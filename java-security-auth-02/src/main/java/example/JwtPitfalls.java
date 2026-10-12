// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.JwtPitfalls
package example;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SingleKeyJWSKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtPitfalls {
    static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    static final String ISSUER = "https://auth.example";
    static final String AUDIENCE = "api-orders";

    static String b64(String text) {
        return B64.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    static String unb64(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }

    static String claim(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"?([^\",}]*)").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    // ===== 甘い検証(よくない例) =====
    // ヘッダーの alg を信じて、署名の検証の方法を選ぶ。iss と aud は見ない
    static String naiveVerify(String token, RSAPublicKey publicKey) throws Exception {
        String[] parts = token.split("\\.", -1);
        String alg = claim(unb64(parts[0]), "alg");
        String signingInput = parts[0] + "." + parts[1];
        boolean valid;
        if ("none".equalsIgnoreCase(alg)) {
            valid = true; // 署名なしを受け入れてしまう
        } else if ("HS256".equals(alg)) {
            // 公開鍵のバイト列を、HMACの秘密として使ってしまう(アルゴリズムの混同)
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(publicKey.getEncoded(), "HmacSHA256"));
            valid = MessageDigest.isEqual(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)), Base64.getUrlDecoder().decode(parts[2]));
        } else {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            valid = signature.verify(Base64.getUrlDecoder().decode(parts[2]));
        }
        if (!valid) return "拒否: 署名が一致しない";
        return "受理: " + unb64(parts[1]);
    }

    // ===== ライブラリでの検証(直した例) =====
    // 署名のアルゴリズムと鍵を固定し、iss は一致、aud は含むことを求め、sub と exp を必須にする
    static DefaultJWTProcessor<SecurityContext> processor(RSAPublicKey publicKey) {
        DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        processor.setJWSKeySelector(new SingleKeyJWSKeySelector<>(JWSAlgorithm.RS256, publicKey));
        processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                Set.of(AUDIENCE),
                new JWTClaimsSet.Builder().issuer(ISSUER).build(),
                Set.of("sub", "exp", "iss", "aud"),
                null));
        return processor;
    }

    static String safeVerify(String token, RSAPublicKey publicKey) {
        try {
            JWTClaimsSet claims = processor(publicKey).process(token, null);
            return "受理: sub=" + claims.getSubject() + " role=" + claims.getClaim("role");
        } catch (Exception e) {
            return "拒否: " + e.getMessage();
        }
    }

    static JWTClaimsSet claims(String issuer, String audience) {
        return new JWTClaimsSet.Builder().subject("alice").claim("role", "user").issuer(issuer).audience(audience)
                .expirationTime(new Date(System.currentTimeMillis() + 300_000)).build();
    }

    static String signRs256(JWTClaimsSet claims, KeyPair keys) throws JOSEException {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(keys.getPrivate()));
        return jwt.serialize();
    }

    public static void main(String[] args) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keys = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keys.getPublic();

        // 攻撃者が作るトークン。攻撃者が知っているのは、公開されている公開鍵だけ
        String adminPayload = "{\"sub\":\"mallory\",\"role\":\"admin\",\"iss\":\"" + ISSUER + "\",\"aud\":\"" + AUDIENCE + "\",\"exp\":9999999999}";
        String noneToken = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "." + b64(adminPayload) + ".";
        String hsInput = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}") + "." + b64(adminPayload);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(publicKey.getEncoded(), "HmacSHA256"));
        String confusedToken = hsInput + "." + B64.encodeToString(mac.doFinal(hsInput.getBytes(StandardCharsets.US_ASCII)));

        // 正しい署名だが、別のAPI向け(iss、aud が違う)のトークン
        String otherAudience = signRs256(claims("https://other-issuer.example", "api-billing"), keys);
        String valid = signRs256(claims(ISSUER, AUDIENCE), keys);

        System.out.println("## 甘い検証");
        System.out.println("  正しいトークン       : " + naiveVerify(valid, publicKey).replaceAll("\"exp\":\\d+", "\"exp\":…"));
        System.out.println("  alg=none            : " + naiveVerify(noneToken, publicKey));
        System.out.println("  アルゴリズムの混同  : " + naiveVerify(confusedToken, publicKey));
        System.out.println("  別のiss/aud向け     : " + naiveVerify(otherAudience, publicKey).replaceAll("\"exp\":\\d+", "\"exp\":…"));

        System.out.println("## ライブラリ(Nimbus JOSE+JWT)で、アルゴリズム・鍵・iss・audを固定");
        System.out.println("  正しいトークン       : " + safeVerify(valid, publicKey));
        System.out.println("  alg=none            : " + safeVerify(noneToken, publicKey));
        System.out.println("  アルゴリズムの混同  : " + safeVerify(confusedToken, publicKey));
        System.out.println("  別のiss/aud向け     : " + safeVerify(otherAudience, publicKey));
    }
}
