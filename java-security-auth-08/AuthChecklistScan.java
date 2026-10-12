// 実行: java AuthChecklistScan.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class AuthChecklistScan {
    // シリーズで再現した問題の「兆候」を、文字列で探すだけの粗い点検。見つかった行は、人が確かめる
    record Rule(String id, String owasp, Pattern pattern, String advice) {}

    static final List<Rule> RULES = List.of(
            new Rule("#02", "A07", Pattern.compile("\"none\"\\.equalsIgnoreCase|equalsIgnoreCase\\(\"none\"\\)"), "alg=none を受け入れていないか。アルゴリズムは受け取る側が固定する"),
            new Rule("#04", "A07", Pattern.compile("JwtValidators\\.createDefault\\(\\)"), "期限などの検証だけで、iss と aud を検証しない。createDefaultWithIssuer を使い、aud の検証も足す"),
            new Rule("#02", "A07", Pattern.compile("getHeader\\(\\).*getAlgorithm\\(\\).*(decoder|Decoder|verify)"), "トークンのalgで検証の方法を選んでいないか確かめる"),
            new Rule("#04", "A07", Pattern.compile("new JwtTimestampValidator\\(Duration\\.of(Minutes|Hours)"), "時計のずれの許容が長すぎないか。短い有効期間のトークンでは期限の意味が薄れる"),
            new Rule("#06", "A07", Pattern.compile("reuseRefreshTokens\\(true\\)"), "リフレッシュトークンを使い回している。ローテーションを検討する"),
            new Rule("#07", "A07", Pattern.compile("(redirect\\w*|requested\\w*)\\.startsWith\\(|\\.startsWith\\(\\w*[Rr]edirect"), "redirect_uri を前方一致で検証していないか。完全一致にする"),
            new Rule("#07", "A04", Pattern.compile("[?&]access_token=|\"/\\*\\*\"\\)\\.permitAll"), "トークンをURLに付けていないか、全部のURLを許可していないか確かめる"),
            new Rule("#07", "A02", Pattern.compile("\\{noop\\}"), "平文のシークレット({noop})。実験用の値で、本番ではハッシュ化し、環境変数などから渡す"));

    public static void main(String[] args) throws IOException {
        Path dir = Files.createTempDirectory("scan-sample-auth");
        Files.writeString(dir.resolve("BadAuth.java"), """
                class BadAuth {
                    boolean verify(String alg, String redirectUri) {
                        if ("none".equalsIgnoreCase(alg)) {
                            return true;
                        }
                        return redirectUri.startsWith("https://client.example/callback");
                    }

                    JwtDecoder decoder() {
                        decoder.setJwtValidator(JwtValidators.createDefault());
                        validator = new JwtTimestampValidator(Duration.ofMinutes(10));
                        return decoder;
                    }

                    String url(String token) {
                        return "https://api.example/orders?access_token=" + token;
                    }

                    void register() {
                        TokenSettings.builder().reuseRefreshTokens(true).build();
                        builder.clientSecret("{noop}secret");
                    }
                }
                """);
        try (var files = Files.list(dir)) {
            for (Path file : files.sorted().toList()) {
                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++) {
                    for (Rule rule : RULES) {
                        if (rule.pattern().matcher(lines.get(i)).find()) {
                            System.out.printf("%s:%d [%s/%s] %s%n    %s%n",
                                    file.getFileName(), i + 1, rule.id(), rule.owasp(), rule.advice(), lines.get(i).strip());
                        }
                    }
                }
            }
        }
    }
}
