// 実行: java JwtDemo.java
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtDemo {
    static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    static final byte[] SECRET = "demo-secret-key-for-experiment-only!".getBytes(StandardCharsets.UTF_8); // 実験用の仮の値
    static final long NOW = 1_700_000_000L; // 実験を毎回同じ結果にするため、現在時刻を固定する

    static String b64(String text) {
        return B64.encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    static String unb64(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }

    static byte[] hmac(String signingInput) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET, "HmacSHA256"));
        return mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
    }

    // HS256: ヘッダー.ペイロード を共通の秘密でHMACして、署名にする
    static String signHs256(String payloadJson) throws Exception {
        String input = b64("{\"alg\":\"HS256\",\"typ\":\"JWT\"}") + "." + b64(payloadJson);
        return input + "." + B64.encodeToString(hmac(input));
    }

    static String signRs256(String payloadJson, KeyPair keys) throws Exception {
        String input = b64("{\"alg\":\"RS256\",\"typ\":\"JWT\"}") + "." + b64(payloadJson);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keys.getPrivate());
        signature.update(input.getBytes(StandardCharsets.US_ASCII));
        return input + "." + B64.encodeToString(signature.sign());
    }

    static boolean signatureValidHs256(String token) throws Exception {
        String[] parts = token.split("\\.");
        byte[] expected = hmac(parts[0] + "." + parts[1]);
        return MessageDigest.isEqual(expected, Base64.getUrlDecoder().decode(parts[2]));
    }

    static boolean signatureValidRs256(String token, PublicKey key) throws Exception {
        String[] parts = token.split("\\.");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
        return signature.verify(Base64.getUrlDecoder().decode(parts[2]));
    }

    // 有効期限(exp: 1970年からの秒数)を確かめる。実験用に、正規表現で取り出す簡易な実装
    static String check(String token) throws Exception {
        if (!signatureValidHs256(token)) return "拒否: 署名が一致しない";
        Matcher m = Pattern.compile("\"exp\":(\\d+)").matcher(unb64(token.split("\\.")[1]));
        if (!m.find()) return "拒否: exp がない";
        if (Long.parseLong(m.group(1)) <= NOW) return "拒否: 期限切れ";
        return "受理: " + unb64(token.split("\\.")[1]);
    }

    public static void main(String[] args) throws Exception {
        String payload = "{\"sub\":\"alice\",\"role\":\"user\",\"exp\":" + (NOW + 300) + "}";
        String token = signHs256(payload);
        String[] parts = token.split("\\.");

        System.out.println("## 1. HS256のトークンの構造");
        System.out.println("  トークン  : " + token);
        System.out.println("  ヘッダー  : " + unb64(parts[0]));
        System.out.println("  ペイロード: " + unb64(parts[1]));
        System.out.println("  署名の長さ: " + Base64.getUrlDecoder().decode(parts[2]).length + " バイト");

        System.out.println("## 2. 鍵がなくても、ペイロードは読める");
        System.out.println("  " + unb64(token.split("\\.")[1]));

        System.out.println("## 3. 検証");
        System.out.println("  正しいトークン        : " + check(token));
        String tamperedPayload = "{\"sub\":\"alice\",\"role\":\"admin\",\"exp\":" + (NOW + 300) + "}";
        String tampered = parts[0] + "." + b64(tamperedPayload) + "." + parts[2];
        System.out.println("  roleを書き換えたもの  : " + check(tampered));
        String expired = signHs256("{\"sub\":\"alice\",\"role\":\"user\",\"exp\":" + (NOW - 1) + "}");
        System.out.println("  期限切れ(署名は正しい): " + check(expired));
        String noExp = signHs256("{\"sub\":\"alice\",\"role\":\"user\"}");
        System.out.println("  expがないもの         : " + check(noExp));

        System.out.println("## 4. RS256: 秘密鍵で署名し、公開鍵で検証する");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keys = generator.generateKeyPair();
        String rsToken = signRs256(payload, keys);
        String[] rsParts = rsToken.split("\\.");
        System.out.println("  ヘッダー        : " + unb64(rsParts[0]));
        System.out.println("  署名の長さ      : " + Base64.getUrlDecoder().decode(rsParts[2]).length + " バイト");
        System.out.println("  公開鍵で検証    : " + signatureValidRs256(rsToken, keys.getPublic()));
        String rsTampered = rsParts[0] + "." + b64(tamperedPayload) + "." + rsParts[2];
        System.out.println("  書き換えたもの  : " + signatureValidRs256(rsTampered, keys.getPublic()));
        KeyPair other = generator.generateKeyPair();
        System.out.println("  別の鍵で検証    : " + signatureValidRs256(rsToken, other.getPublic()));
    }
}
