// 実行: java SecureTokens.java
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureTokens {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        System.out.println("SecureRandomのアルゴリズム: " + random.getAlgorithm());

        // 16バイト(128ビット)のランダムな値を、URLに使える文字列にする
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        System.out.println("トークンの例: " + token + " (" + token.length() + "文字)");

        // 推測の難しさの見積もり:128ビットの値を、1秒に10億回試せるとして
        BigInteger combinations = BigInteger.TWO.pow(128);
        BigInteger perSecond = BigInteger.valueOf(1_000_000_000L);
        BigInteger seconds = combinations.divide(perSecond);
        BigInteger years = seconds.divide(BigInteger.valueOf(60L * 60 * 24 * 365));
        System.out.println("128ビットの組み合わせの数: " + combinations);
        System.out.println("全部を試すのに必要な年数(毎秒10億回): 約 " + years);

        // 比較:Randomで作った32ビットのトークンは、全部を試しても
        BigInteger small = BigInteger.TWO.pow(32);
        System.out.println("32ビットの組み合わせの数: " + small + " (毎秒10億回なら " + small.divide(perSecond) + " 秒)");
    }
}
